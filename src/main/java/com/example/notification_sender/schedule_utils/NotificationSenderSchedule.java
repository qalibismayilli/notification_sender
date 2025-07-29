package com.example.notification_sender.schedule_utils;


import com.example.notification_sender.client.SmsClient;
import com.example.notification_sender.dto.NotificationDto;
import com.example.notification_sender.mapper.NotificationMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class NotificationSenderSchedule {

    private final NotificationMapper notificationMapper;
    private final SmsClient client;
    private final JavaMailSender emailSender;

    @Value("${sms.username}")
    private String username;

    @Value("${sms.password}")
    private String password;

    @Value("${sms.senderName}")
    private String senderName;

    private static byte[] cachedImageBytes = null;


    @Scheduled(fixedDelay = 120000)
    public void smsSender() {
        try {
            List<NotificationDto> notifications = notificationMapper.getUnSentSmsNotifications();

            for (NotificationDto notification : notifications) {
                String phoneNumber = notification.getContactContent();
                try {
                    String content = buildSmsContent(notification);
                    client.sendSms(username, password, phoneNumber, senderName, content);
                    notificationMapper.toMarkAsSentNotificationQueue(notification.getNotificationQueueId());
                } catch (Exception e) {
                    e.printStackTrace();
                    notificationMapper.toMarkAsUnSentLevel2(notification.getNotificationQueueId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedDelay = 240000)
    public void emailSender() {
        try {
            List<NotificationDto> notifications = notificationMapper.getUnSentEmailNotifications();
            notifications.forEach(notification -> {
                try {
                    String subject = "Alarm - " + notification.getCategoryTitle();

                    sendHtmlEmailWithImage(
                            notification.getContactContent(),
                            subject,
                            buildEmailContent(notification),
                            "http://192.168.121.3:8181/" + notification.getDestinationPath()
                    );
                    notificationMapper.toMarkAsSentNotificationQueue(notification.getNotificationQueueId());
                } catch (Exception e) {
                    e.printStackTrace();
                    notificationMapper.toMarkAsUnSentLevel2(notification.getNotificationQueueId());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendHtmlEmailWithImage(String to, String subject, String contentText, String imagePath) throws MessagingException, IOException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setFrom("alarm@ayes.az");

        String cid = UUID.randomUUID().toString();

        String htmlContent =
                "<h3>Alarm:</h3>"
                        + "<p>" + contentText + "</p>"
                        + "<img src='cid:" + cid + "' style='width: 400px; height: auto; display: block; margin-top: 10px; margin-bottom: 10px;' />"
                        + "<p><a href='https://facelab.ayes.az'>facelab.ayes.az</a></p>";

        helper.setText(htmlContent, true);

        try {
            URL url = new URL(imagePath);
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            InputStream in = connection.getInputStream();
            byte[] imageBytes = in.readAllBytes();

            final String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
            final String mimeType = fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ? "image/jpeg" : "image/png";

            System.out.println(">>> imagePath: " + imagePath);
            System.out.println(">>> imageBytes.length: " + imageBytes.length);
            System.out.println(">>> imageHash: " + Arrays.hashCode(imageBytes));

            ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };
            helper.addInline(cid, imageResource, mimeType);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        emailSender.send(message);
    }

    private String buildSmsContent(NotificationDto dto) {
        return dto.getCreateDate() +
                " - " + dto.getCategoryTitle() +
                " - " + dto.getFullName() +
                " - " + dto.getParentBranchTitle() +
                " - " + dto.getBranchTitle() +
                " - " + dto.getCameraTitle() + " kamerası";
    }

    private String buildEmailContent(NotificationDto dto) {
        return dto.getCreateDate() +
                " tarixində " + dto.getFullName() +
                " " + dto.getParentBranchTitle() +
                " filialı, " + dto.getBranchTitle() +
                " söbəsi, " + dto.getCameraTitle() +
                " kamerasından keçdi.";
    }

    private byte[] downloadImage(String ftpImageUrl) throws IOException {
        if (cachedImageBytes != null) {
            return cachedImageBytes;
        }

        URL url = new URL(ftpImageUrl);
        try (InputStream inputStream = url.openStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            cachedImageBytes = outputStream.toByteArray();
            return cachedImageBytes;
        }
    }

}
