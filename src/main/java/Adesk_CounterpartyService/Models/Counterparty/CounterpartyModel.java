package Adesk_CounterpartyService.Models.Counterparty;

import Adesk_CounterpartyService.Models.Category.CounterpartyCategoryModel;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
public class CounterpartyModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "email")
    private String email; //возможно, нужно добавить валидацию почты и тп

    @Column(name = "number")
    private String number;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "counterparty_categories_relation",
        joinColumns = @JoinColumn(name = "counterparty_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<CounterpartyCategoryModel> categories = new HashSet<>();
}
