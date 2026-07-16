package com.artur_sres.multinova.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "brands")
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String name;


    public Long getId() {return id;}

    public User getUser() {return user;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
}
