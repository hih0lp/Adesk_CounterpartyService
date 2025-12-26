package Adesk_CounterpartyService.Models.Category;


import Adesk_CounterpartyService.Models.Counterparty.CounterpartyModel;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
public class CounterpartyCategoryModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "company_id")
    private Long companyId;

    @ManyToMany(mappedBy = "categories")
    private Set<CounterpartyModel> counterparties = new HashSet<>();
}
