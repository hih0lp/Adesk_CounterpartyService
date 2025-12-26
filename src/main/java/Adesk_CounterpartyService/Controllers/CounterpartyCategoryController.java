package Adesk_CounterpartyService.Controllers;


import Adesk_CounterpartyService.Models.Category.CounterpartyCategoryDTO;
import Adesk_CounterpartyService.Models.Category.CounterpartyCategoryModel;
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
@RequiredArgsConstructor
@RequestMapping("/counterparty/categories")
public class CounterpartyCategoryController {
    private final CounterpartyCategoriesRepository _counterpartyCategoriesRepository;
    private final CounterpartyRepository _counterpartyRepository;
    private final Logger log = LoggerFactory.getLogger(CounterpartyCategoryController.class);



    @PostMapping("/create-category")
    public ResponseEntity<?> createCounterpartyCategory(@RequestBody CounterpartyCategoryDTO dto, HttpServletRequest request){
        try{
            if(!dto.isValid())
                return ResponseEntity.badRequest().body("invalid data");

            if(request.getHeader("X-Authenticated") == null || request.getHeader("X-Authenticated").isEmpty())
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("request has been came not from gateway");

            if(!Arrays.stream(request.getHeader("X-User-Permissions").split(",")).anyMatch(s -> s.equals("COUNTERPARTY_WORK")))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("no rights");

            var counterpartyCategoryOpt = _counterpartyCategoriesRepository.findByNameAndCompanyId(dto.name, Long.parseLong(request.getHeader("X-Company-Id")));
            if(counterpartyCategoryOpt.isPresent())
                return ResponseEntity.badRequest().body("category already exist");

            var newCategory = new CounterpartyCategoryModel();
            newCategory.setName(dto.getName());
            newCategory.setCompanyId(Long.parseLong(request.getHeader("X-Company-Id")));

            _counterpartyCategoriesRepository.save(newCategory);

            return ResponseEntity.ok().body("Successfully creating counterparty category");
        } catch(Exception ex) {
            log.error(ex.getMessage());
            return ResponseEntity.status(500).body("Logic error");
        }
    }



    @DeleteMapping("/delete-category")
    public ResponseEntity<?> deleteCounterpartyCategory(@RequestBody CounterpartyCategoryDTO dto, HttpServletRequest request){
        try{
            if(!dto.isValid())
                return ResponseEntity.badRequest().body("invalid data");

            if(request.getHeader("X-Authenticated") == null || request.getHeader("X-Authenticated").isEmpty())
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("request has been came not from gateway");

            if(!Arrays.stream(request.getHeader("X-User-Permissions").split(",")).anyMatch(s -> s.equals("COUNTERPARTY_WORK")))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("no rights");

            var counterpartyCategoryOpt = _counterpartyCategoriesRepository.findByNameAndCompanyId(dto.name, Long.parseLong(request.getHeader("X-Company-Id")));
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
