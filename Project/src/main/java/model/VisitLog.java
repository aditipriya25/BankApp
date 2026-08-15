package model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "visit_log")
public class VisitLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "booking_id")
    private String bookingId;

    @Column(name = "logged_by_employee_id")
    private String loggedByEmployeeId;

    @Column(name = "key_issued_at")
    private LocalDateTime keyIssuedAt;

    @Column(name = "key_returned_at")
    private LocalDateTime keyReturnedAt;

    public VisitLog() {
    }

    public VisitLog(String id, String bookingId, String loggedByEmployeeId, LocalDateTime keyIssuedAt, LocalDateTime keyReturnedAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.loggedByEmployeeId = loggedByEmployeeId;
        this.keyIssuedAt = keyIssuedAt;
        this.keyReturnedAt = keyReturnedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getLoggedByEmployeeId() {
        return loggedByEmployeeId;
    }

    public void setLoggedByEmployeeId(String loggedByEmployeeId) {
        this.loggedByEmployeeId = loggedByEmployeeId;
    }

    public LocalDateTime getKeyIssuedAt() {
        return keyIssuedAt;
    }

    public void setKeyIssuedAt(LocalDateTime keyIssuedAt) {
        this.keyIssuedAt = keyIssuedAt;
    }

    public LocalDateTime getKeyReturnedAt() {
        return keyReturnedAt;
    }

    public void setKeyReturnedAt(LocalDateTime keyReturnedAt) {
        this.keyReturnedAt = keyReturnedAt;
    }
}