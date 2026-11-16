package com.mockProject.group6.entity;


import jakarta.persistence.*;

@Entity
@Table(name="Role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name", nullable = false, unique = true, length = 30)
    private String name;

    @Column(name="description", length = 255)
    private String description;

    @Column(name="status", nullable = false)
    private Boolean status = true;

    public Role() {}

    public Role(int id, Boolean status, String description, String name) {
        this.id = id;
        this.status = status;
        this.description = description;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}