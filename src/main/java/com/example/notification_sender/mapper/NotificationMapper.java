package com.example.notification_sender.mapper;

import com.example.notification_sender.dto.NotificationDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;


@Mapper
public interface NotificationMapper {

    @Select("""
            select nq.notification_queue_id as notificationQueueId,
            nq.person_id as personId,
            concat(p.first_name,' ',p.last_name) as fullName,
            SUBSTRING(CONCAT(f2.file_path, '/', f2.file_name), 22) AS imageUrl,
            nq.category_id as categoryId,
            pc.category_name as categoryTitle,
            nq.sub_branch_id as branchId,
            b.title as branchTitle,
            b.parent_branch_id as parentBranchId,
            b2.title as parentBranchTitle,
            nq.save_date as createDate,
            nq.receiver_emp_id as receiverEmpId,
            concat(u.first_name,' ',u.last_name) as receiverEmpFullName,
            nq.contact_content as contactContent,
            nq.camera_id as cameraId,
            c.camera_name as cameraTitle,
            SUBSTRING(CONCAT(d.destination_path, '/', d.file_name), 22) AS destinationPath
            from notification_queue nq
            left join persons p on p.person_id = nq.person_id
            left join cameras c on c.camera_id = nq.camera_id
            LEFT JOIN
            (
             SELECT DISTINCT ON (pi.person_id)
             pi.person_id,
             f.file_path,
             f.file_name
             FROM person_images pi
             LEFT JOIN files f ON f.file_id = pi.image_id
             WHERE pi.is_deleted = false
             ORDER BY pi.person_id, pi.person_image_id
            ) f2 ON f2.person_id = nq.person_id
            left join branches b on b.branch_id = nq.sub_branch_id
            left join branches b2 on b2.branch_id = b.parent_branch_id
            left join users u on u.user_id = nq.receiver_emp_id
            left join person_categories pc on pc.person_category_id = nq.category_id
            left join destination d on nq.destination_id = d.destination_id
            where
            nq.status_id=0 and
            nq.notification_type='SMS' and
            nq.is_active=1
            order by nq.save_date
            """)
    List<NotificationDto> getUnSentSmsNotifications();

    @Select("""
            select nq.notification_queue_id as notificationQueueId,
            nq.person_id as personId,
            concat(p.first_name,' ',p.last_name) as fullName,
            SUBSTRING(CONCAT(f2.file_path, '/', f2.file_name), 22) AS imageUrl,
            nq.category_id as categoryId,
            pc.category_name as categoryTitle,
            nq.sub_branch_id as branchId,
            b.title as branchTitle,
            b.parent_branch_id as parentBranchId,
            b2.title as parentBranchTitle,
            nq.save_date as createDate,
            nq.receiver_emp_id as receiverEmpId,
            concat(u.first_name,' ',u.last_name) as receiverEmpFullName,
            nq.contact_content as contactContent,
            nq.camera_id as cameraId,
            c.camera_name as cameraTitle,
            SUBSTRING(CONCAT(d.destination_path, '/', d.file_name), 22) AS destinationPath
            from notification_queue nq
            left join persons p on p.person_id = nq.person_id
            left join cameras c on c.camera_id = nq.camera_id
            LEFT JOIN
            (
             SELECT DISTINCT ON (pi.person_id)
             pi.person_id,
             f.file_path,
             f.file_name
             FROM person_images pi
             LEFT JOIN files f ON f.file_id = pi.image_id
             WHERE pi.is_deleted = false
             ORDER BY pi.person_id, pi.person_image_id
            ) f2 ON f2.person_id = nq.person_id
            left join branches b on b.branch_id = nq.sub_branch_id
            left join branches b2 on b2.branch_id = b.parent_branch_id
            left join users u on u.user_id = nq.receiver_emp_id
            left join person_categories pc on pc.person_category_id = nq.category_id
            left join destination d on nq.destination_id = d.destination_id
            where
            nq.status_id=0 and
            nq.notification_type='EMAIL' and
            nq.is_active=1 
            order by nq.save_date
            """)
    List<NotificationDto> getUnSentEmailNotifications();


    @Update("""
            update notification_queue 
            set status_id = 1, send_date = now()
            where notification_queue_id = #{notificationQueueId}
            """)
    void toMarkAsSentNotificationQueue(Long notificationQueueId);


    @Update("""
            update notification_queue 
            set status_id = 2
            where notification_queue_id = #{notificationQueueId}
            """)
    void toMarkAsUnSentLevel2(Long notificationQueueId);
}
