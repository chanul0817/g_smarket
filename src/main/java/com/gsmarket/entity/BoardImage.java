
package com.gsmarket.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Embeddable
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString(exclude = "board") //연관 관계시 항상 주의
public class BoardImage extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inum;

    @Column
    private String uuid;

    @Column
    private String imgName;

    private String path;

    @ManyToOne(fetch = FetchType.LAZY) //무조건 lazy로
    private Board board;


}
