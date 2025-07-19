package com.BE.QuitCare.service;

import com.BE.QuitCare.dto.AppointmentResponseDTO2;
import com.BE.QuitCare.dto.request.AppointmentRequest;
import com.BE.QuitCare.dto.response.AppointmentCoachResponseDTO;
import com.BE.QuitCare.dto.response.AppointmentResponseDTO;
import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.entity.Appointment;
import com.BE.QuitCare.entity.SessionUser;
import com.BE.QuitCare.entity.UserMembership;
import com.BE.QuitCare.enums.AppointmentEnum;
import com.BE.QuitCare.enums.MembershipStatus;
import com.BE.QuitCare.enums.Role;
import com.BE.QuitCare.exception.BadRequestException;
import com.BE.QuitCare.repository.AppointmentRepository;
import com.BE.QuitCare.repository.AuthenticationRepository;
import com.BE.QuitCare.repository.SessionUserRepository;
import com.BE.QuitCare.repository.UserMembershipRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

@Service
public class AppointmentService
{
    @Autowired
    AppointmentRepository appointmentRepository;
    @Autowired
    SessionUserRepository sessionUserRepository;
    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    AuthenticationRepository authenticationRepository;
    @Autowired
    SessionService sessionService;
    @Autowired
    GoogleMeetService  googleMeetService;
    @Autowired
    UserMembershipRepository userMembershipRepository;

    @Transactional
    public AppointmentResponseDTO2 create(AppointmentRequest appointmentRequest) {
        Account customer = authenticationService.getCurentAccount();

        if (customer.getRole() != Role.CUSTOMER) {
            throw new BadRequestException("Chỉ CUSTOMER mới có thể đặt lịch hẹn.");
        }

        Account coach = authenticationRepository.findById(appointmentRequest.getCoachId())
                .orElseThrow(() -> new BadRequestException("Coach not found"));

        if (coach.getRole() != Role.COACH) {
            throw new BadRequestException("Account is not a Coach");
        }

        // Đảm bảo Coach có slot vào ngày chỉ định
        sessionService.ensureSessionForCoachOnDate(coach, appointmentRequest.getAppointmentDate());

        SessionUser slot = sessionUserRepository.findByAccountAndDateAndStart(
                coach,
                appointmentRequest.getAppointmentDate(),
                appointmentRequest.getStart()
        ).orElseThrow(() -> new BadRequestException("Không tìm thấy slot phù hợp"));

        if (!slot.isAvailable()) {
            throw new BadRequestException("Slot is not available");
        }

        // Kiểm tra không được đặt slot ở quá khứ
        LocalDateTime slotDateTime = LocalDateTime.of(slot.getDate(), slot.getStart());
        if (slotDateTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Không thể đặt lịch trong quá khứ.");
        }

        // Lấy ngày đặt để kiểm tra gói còn hiệu lực
        LocalDateTime appointmentDateTime = LocalDateTime.of(slot.getDate(), slot.getStart());

        // Tìm gói membership đang hoạt động tại thời điểm này
        UserMembership membership = userMembershipRepository
                .findFirstByAccount_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateDesc(
                        customer.getId(),
                        appointmentDateTime,
                        appointmentDateTime
                )
                .orElseThrow(() -> new BadRequestException("Bạn chưa có gói thành viên hợp lệ."));

        // Đếm số lần đặt lịch đã dùng của gói này
        int appointmentCount = appointmentRepository.countByUserMembership_Id(membership.getId());

        if (appointmentCount + 1 >= 4) {

            membership.setStatus(MembershipStatus.INACTIVE);
            userMembershipRepository.save(membership);
            throw new BadRequestException("Bạn chỉ được đặt tối đa 4 cuộc hẹn trong thời gian gói.");
        }

        // Tạo cuộc hẹn
        Appointment appointment = new Appointment();
        appointment.setCreateAt(LocalDate.now());
        appointment.setStatus(AppointmentEnum.PENDING);
        appointment.setAccount(customer);
        appointment.setSessionUser(slot);
        appointment.setUserMembership(membership); // GẮN GÓI vào cuộc hẹn

        try {
            String meetLink = googleMeetService.createGoogleMeetLinkFromSlot(slot, coach);
            appointment.setGoogleMeetLink(meetLink);
        } catch (Exception e) {
            throw new BadRequestException("Không thể tạo link Google Meet: " + e.getMessage());
        }

        appointmentRepository.save(appointment);

        // Cập nhật slot đã được đặt
        slot.setAvailable(false);
        sessionUserRepository.save(slot);

        return mapToDto(appointment);
    }


    public AppointmentResponseDTO2 mapToDto(Appointment appointment) {
        AppointmentResponseDTO2 dto = new AppointmentResponseDTO2();
        dto.setId(appointment.getId());
        dto.setStatus(appointment.getStatus().name());
        dto.setGoogleMeetLink(appointment.getGoogleMeetLink());
        dto.setCreateAt(appointment.getCreateAt().toString());

        // Customer
        Account customer = appointment.getAccount();
        dto.setCustomerId(customer.getId());
        dto.setCustomerName(customer.getFullName());
        dto.setCustomerEmail(customer.getEmail());

        // Coach (from sessionUser)
        Account coach = appointment.getSessionUser().getAccount();
        dto.setCoachId(coach.getId());
        dto.setCoachName(coach.getFullName());
        dto.setCoachEmail(coach.getEmail());

        // Session
        dto.setSessionDate(appointment.getSessionUser().getDate().toString());
        dto.setSessionStart(appointment.getSessionUser().getStart().toString());
        dto.setSessionEnd(appointment.getSessionUser().getEnd().toString());

        // Membership
        if (appointment.getUserMembership() != null) {
            dto.setMembershipId(appointment.getUserMembership().getId());
            dto.setMembershipPlanName(appointment.getUserMembership().getMembershipPlan().getName());
            dto.setMembershipStart(appointment.getUserMembership().getStartDate().toString());
            dto.setMembershipEnd(appointment.getUserMembership().getEndDate().toString());
        }

        return dto;
    }



//    private String generateGoogleMeetLink() {
//        // Cách đơn giản: tạo chuỗi giả lập như Meet
//        String uniqueId = java.util.UUID.randomUUID().toString().substring(0, 8);
//        return "https://meet.google.com/" + uniqueId;
//    }
//     @Scheduled(fixedRate = 60000) // chạy mỗi 60 giây
//     @Transactional
//     public void autoUpdatePendingAppointmentsToApproved() {
//          LocalDateTime now = LocalDateTime.now();
//
//          List<Appointment> pendingAppointments = appointmentRepository.findByStatus(AppointmentEnum.PENDING);
//
//          for (Appointment appt : pendingAppointments) {
//          LocalDate appointmentDate = appt.getSessionUser().getDate();
//          LocalTime startTime = appt.getSessionUser().getStart();
//          LocalDateTime scheduledDateTime = LocalDateTime.of(appointmentDate, startTime);
//
//        if (!appt.getStatus().equals(AppointmentEnum.APPROVED)
//                && (now.isAfter(scheduledDateTime) || now.isEqual(scheduledDateTime))) {
//            appt.setStatus(AppointmentEnum.APPROVED);
//            appointmentRepository.save(appt);
//        }
//    }
//}


    public List<AppointmentCoachResponseDTO> getAppointmentsForCurrentCoach() {
        Account coach = authenticationService.getCurentAccount();

        if (coach == null) {
            throw new SecurityException("Không thể lấy thông tin Coach đang đăng nhập.");
        }

        if (coach.getRole() != Role.COACH) {
            throw new BadRequestException("Chỉ Coach mới có thể xem lịch hẹn.");
        }

        List<Appointment> appointments = appointmentRepository
                .findBySessionUser_Account_IdOrderByCreateAtDesc(coach.getId());

        return appointments.stream().map(appointment -> {
            AppointmentCoachResponseDTO dto = new AppointmentCoachResponseDTO();
            dto.setId(appointment.getId());
            dto.setCustomerName(appointment.getAccount().getFullName());
            dto.setAppointmentDate(appointment.getSessionUser().getDate());
            dto.setStartTime(appointment.getSessionUser().getStart());
            dto.setStatus(appointment.getStatus().name());
            dto.setGoogleMeetLink(appointment.getGoogleMeetLink());
            return dto;
        }).toList();
    }


    public List<AppointmentResponseDTO> getAppointmentsForCurrentCustomer() {
        Account customer = authenticationService.getCurentAccount();

        if (customer.getRole() != Role.CUSTOMER) {
            throw new BadRequestException("Chỉ CUSTOMER mới có thể xem lịch hẹn của mình.");
        }

        List<Appointment> appointments = appointmentRepository.findByAccount_IdOrderByCreateAtDesc(customer.getId());

        return appointments.stream().map(appt -> {
            AppointmentResponseDTO dto = new AppointmentResponseDTO();
            dto.setCoachName(appt.getSessionUser().getAccount().getFullName());
            dto.setAppointmentDate(appt.getSessionUser().getDate());
            dto.setStartTime(appt.getSessionUser().getStart());
            dto.setStatus(appt.getStatus().name());
            dto.setGoogleMeetLink(appt.getGoogleMeetLink());
            return dto;
        }).toList();
    }

    public void markAsCompleted(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BadRequestException("Appointment not found"));

        // Lấy tài khoản coach hiện tại từ context
        Account currentCoach = authenticationService.getCurentAccount(); // sửa lại tên biến cho đúng nghĩa

        // Kiểm tra coach có phải chủ của sessionUser không
        if (!appointment.getSessionUser().getAccount().getId().equals(currentCoach.getId())) {
            throw new BadRequestException("Bạn không có quyền hoàn tất cuộc hẹn này.");
        }

        // Kiểm tra trạng thái trước khi cập nhật
        if (appointment.getStatus() != AppointmentEnum.PENDING) {
            throw new BadRequestException("Chỉ có thể xác nhận các cuộc hẹn ở trạng thái PENDING.");
        }

        // Cập nhật trạng thái hoàn thành
        appointment.setStatus(AppointmentEnum.COMPLETED);
        appointmentRepository.save(appointment);
    }

    @Transactional
    public void markAsCancel(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy cuộc hẹn."));

        Account currentUser = authenticationService.getCurentAccount();
        Role role = currentUser.getRole();

        boolean isCoach = role == Role.COACH &&
                appointment.getSessionUser().getAccount().getId().equals(currentUser.getId());

        boolean isCustomer = role == Role.CUSTOMER &&
                appointment.getAccount().getId().equals(currentUser.getId());

        if (!isCoach && !isCustomer) {
            throw new BadRequestException("Bạn không có quyền hủy cuộc hẹn này.");
        }

        if (appointment.getStatus() != AppointmentEnum.PENDING) {
            throw new BadRequestException("Chỉ có thể hủy cuộc hẹn đang chờ xác nhận.");
        }

        // Cập nhật trạng thái và giải phóng slot
        appointment.setStatus(AppointmentEnum.CANCELLED);
        appointment.getSessionUser().setAvailable(true);

        appointmentRepository.save(appointment);
        sessionUserRepository.save(appointment.getSessionUser());
    }

}
