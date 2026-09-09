package com.hrms.backend.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "designations")
public class Designation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer designationId;
    private String designationName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Integer getDesignationId() { return designationId; }
    public void setDesignationId(Integer designationId) { this.designationId = designationId; }
    public String getDesignationName() { return designationName; }
    public void setDesignationName(String designationName) { this.designationName = designationName; }
}
