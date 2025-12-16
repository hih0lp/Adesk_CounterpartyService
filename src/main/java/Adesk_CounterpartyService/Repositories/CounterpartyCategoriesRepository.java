package Adesk_CounterpartyService.Repositories;

import Adesk_CounterpartyService.Models.Category.CounterpartyCategoryModel;
import Adesk_CounterpartyService.Models.Counterparty.CounterpartyModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CounterpartyCategoriesRepository extends JpaRepository<CounterpartyCategoryModel, Long> {
    Optional<CounterpartyCategoryModel> findByName(String name);
    Optional<CounterpartyCategoryModel> findByNameAndCompanyId(String userEmail, Long companyId);
}
