package com.markstart.urlshortener.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table( name = "url_mappings" )
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true,  length = 64)
    private String alias;

    @Column(nullable = false,  length = 2048)
    private String fullUrl;

}
