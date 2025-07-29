package com.example.notification_sender.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationDto {

    Long notificationQueueId;
    Long personId;
    String imageUrl;
    String fullName;

    Long categoryId;
    String categoryTitle;

    Long parentBranchId;
    String parentBranchTitle;

    Long branchId;
    String branchTitle;

    Long cameraId;
    String cameraTitle;

    LocalDateTime createDate;

    Long receiverEmpId;
    String receiverEmpFullName;

    String contactContent;
    String destinationPath;
}
