package Adesk_CounterpartyService.Repositories;

import Adesk_CounterpartyService.Models.Counterparty.CounterpartyModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CounterpartyRepository extends JpaRepository<CounterpartyModel, Long> {
    Optional<CounterpartyModel> findByName(String name);
    Optional<CounterpartyModel> findByNameAndCompanyId(String userEmail, Long companyId);
    Optional<CounterpartyModel> findByCompanyId(Long companyId);
}
