package com.yhkim.fave.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(schema = "fave", name = "favorites")
@Getter
@Setter
public class FavoritesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`index`")
    private int index;

    @Column(nullable = false ,length = 50)
    private String userEmail;

    @Column(nullable = false)
    private int festivalId;
}
