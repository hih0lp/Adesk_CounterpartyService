package Adesk_CounterpartyService.Controllers;


import Adesk_CounterpartyService.Models.Category.CounterpartyCategoryDTO;
import Adesk_CounterpartyService.Models.Category.CounterpartyCategoryModel;
import Adesk_CounterpartyService.Models.Counterparty.CounterpartyModel;
import Adesk_CounterpartyService.Models.Counterparty.CounterpartyModelDTO;
import Adesk_CounterpartyService.Repositories.CounterpartyCategoriesRepository;
import Adesk_CounterpartyService.Repositories.CounterpartyRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/counterparty/models")
@RequiredArgsConstructor
public class CounterpartyController {
    private final CounterpartyCategoriesRepository _counterpartyCategoriesRepository;
    private final CounterpartyRepository _counterpartyRepository;
    private final Logger log = LoggerFactory.getLogger(CounterpartyController.class);


    @PostMapping("/create-counterparty")
    public ResponseEntity<?> createCounterparty(@RequestBody CounterpartyModelDTO dto, HttpServletRequest request){
        try{
            if(!dto.isValid())
                return ResponseEntity.badRequest().body("invalid data");

            if(request.getHeader("X-Authenticated") == null || request.getHeader("X-Authenticated").isEmpty())
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("request has been came not from gateway");

            if(!Arrays.stream(request.getHeader("X-User-Permissions").split(",")).anyMatch(s -> s.equals("COUNTERPARTY_WORK")))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("no rights");

            var counterpartyCategoryOpt = _counterpartyCategoriesRepository.findByNameAndCompanyId(dto.getName(), dto.getCompanyId());
            if(counterpartyCategoryOpt.isEmpty()) //если категории не существует
                return ResponseEntity.badRequest().body("category doesn't exist");

            var counterparty = _counterpartyRepository.findByNameAndCompanyId(dto.categoryName, dto.getCompanyId());
            if(counterparty.isPresent()) //если не существует контрагента
                return ResponseEntity.badRequest().body("category already exist");

            var category = counterpartyCategoryOpt.get();

            var newCounterparty = new CounterpartyModel();
            newCounterparty.getCategories().add(category);
            newCounterparty.setName(dto.getName());
            newCounterparty.setEmail(dto.getEmail());
            newCounterparty.setDescription(dto.getDescription());
            newCounterparty.setCompanyId(Long.parseLong(request.getHeader("X-Company-Id")));
            newCounterparty.setEmail(dto.getEmail()); //возможно, тут нужен имелй пользователя, который его добавляет, хз

            _counterpartyRepository.save(newCounterparty);

            return ResponseEntity.ok().body("Successfully creating counterparty category");
        } catch(Exception ex) {
            log.error(ex.getMessage());
            return ResponseEntity.status(500).body("Logic error");
        }
    }


    /// СКОРЕЕ ВСЕГО, УДАЛЕНИЕ ДОЛЖНО БЫТЬ ПО АЙДИ КОМПАНИИ И ИМЕНИ
    @DeleteMapping("/delete-counterparty/{name}")
    public ResponseEntity<?> deleteCounterpartyCategory(@PathVariable String name, HttpServletRequest request){
        try{
            if(name.isEmpty())
                return ResponseEntity.badRequest().body("invalid data");

            if(request.getHeader("X-Authenticated") == null || request.getHeader("X-Authenticated").isEmpty())
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("request has been came not from gateway");

            if(!Arrays.stream(request.getHeader("X-User-Permissions").split(",")).anyMatch(s -> s.equals("COUNTERPARTY_WORK")))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("no rights");

            var counterpartyCategoryOpt = _counterpartyCategoriesRepository
                    .findByNameAndCompanyId(name, Long.parseLong(request.getHeader("X-Company-Id")));

            if(counterpartyCategoryOpt.isEmpty())
                return ResponseEntity.badRequest().body("category doesn't exist");

            var category = counterpartyCategoryOpt.get();

            _counterpartyCategoriesRepository.delete(category);

            return ResponseEntity.ok().body("Successfully creating counterparty category");
        } catch(Exception ex) {
            log.error(ex.getMessage());
            return ResponseEntity.status(500).body("Logic error");
        }
    }


}
