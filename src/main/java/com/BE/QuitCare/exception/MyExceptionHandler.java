package com.BE.QuitCare.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MyExceptionHandler {

    // mục tiêu: bắt lỗi và return message về cho phía FE
    // MethodArgumentNotValidException
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleBadRequestException(MethodArgumentNotValidException exception){
        System.out.println("Người dùng nhập chưa đúng thông tin");
        String responseMessage = "";

        for(FieldError fieldError: exception.getFieldErrors()){
            responseMessage += fieldError.getDefaultMessage() + "\n";
        }

        return new ResponseEntity(responseMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity handAuthenticaitonEcxception(AuthenticationException exception){
        return new ResponseEntity(exception.getMessage(), HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity handleBadRequestException(BadRequestException exception){
        return new ResponseEntity(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Bắt EntityNotFoundException và trả về HttpStatus.NOT_FOUND (404).
     * Thường xảy ra khi không tìm thấy tài nguyên (ví dụ: Account, MembershipPlan, PaymentHistory).
     * @param exception EntityNotFoundException được ném ra.
     * @return ResponseEntity chứa thông báo lỗi và HttpStatus.NOT_FOUND.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException exception){
        System.err.println("Lỗi Entity Not Found: " + exception.getMessage()); // Ghi log lỗi server
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Bắt IllegalArgumentException và trả về HttpStatus.BAD_REQUEST (400).
     * Thường xảy ra khi có lỗi logic nghiệp vụ hoặc tham số không hợp lệ.
     * @param exception IllegalArgumentException được ném ra.
     * @return ResponseEntity chứa thông báo lỗi và HttpStatus.BAD_REQUEST.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException exception){
        System.err.println("Lỗi tham số không hợp lệ/logic nghiệp vụ: " + exception.getMessage()); // Ghi log lỗi server
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Bắt SecurityException và trả về HttpStatus.FORBIDDEN (403).
     * Thường xảy ra khi người dùng không có quyền thực hiện hành động.
     * (Nếu bạn có SecurityException trong service, hãy thêm handler này)
     * @param exception SecurityException được ném ra.
     * @return ResponseEntity chứa thông báo lỗi và HttpStatus.FORBIDDEN.
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> handleSecurityException(SecurityException exception){
        System.err.println("Lỗi bảo mật/quyền truy cập: " + exception.getMessage()); // Ghi log lỗi server
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.FORBIDDEN);
    }

    // --- HANDLER CHUNG CHO TẤT CẢ CÁC LỖI KHÔNG ĐƯỢC BẮT CỤ THỂ ---
    /**
     * Bắt tất cả các Exception không được xử lý cụ thể và trả về HttpStatus.INTERNAL_SERVER_ERROR (500).
     * @param exception Exception chung.
     * @return ResponseEntity chứa thông báo lỗi chung và HttpStatus.INTERNAL_SERVER_ERROR.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception exception){
        System.err.println("Lỗi server không xác định: " + exception.getMessage()); // Ghi log lỗi server
        exception.printStackTrace(); // In stack trace để debug
        return new ResponseEntity<>("Đã xảy ra lỗi không xác định. Vui lòng thử lại sau.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}



