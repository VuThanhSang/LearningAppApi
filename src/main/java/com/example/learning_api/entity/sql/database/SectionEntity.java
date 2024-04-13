package com.example.learning_api.entity.sql.database;

import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "sections")
public class SectionEntity {
    @Id
    private String id;
    @DBRef
    private ClassRoomEntity classRoom;
    private String name;
    private String description;
    private int order;
    private Date createdAt;
    private Date updatedAt;
    @DBRef
    private List<ResourceEntity> resources;
    @DBRef
    private List<SubstanceEntity> substances;
    @DBRef
    private List<TestEntity> tests;
}
