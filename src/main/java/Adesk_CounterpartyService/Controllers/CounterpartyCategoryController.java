package Adesk_CounterpartyService.Controllers;

import Adesk_CounterpartyService.Models.Category.CounterpartyCategoryDTO;
import Adesk_CounterpartyService.Models.Category.CounterpartyCategoryModel;
import Adesk_CounterpartyService.Repositories.CounterpartyCategoriesRepository;
import Adesk_CounterpartyService.Repositories.CounterpartyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Категории контрагентов", description = "API для управления категориями контрагентов")
@SecurityRequirement(name = "bearerAuth")
public class CounterpartyCategoryController {
    private final CounterpartyCategoriesRepository _counterpartyCategoriesRepository;
    private final CounterpartyRepository _counterpartyRepository;
    private final Logger log = LoggerFactory.getLogger(CounterpartyCategoryController.class);

    @PostMapping("/create-category")
    @Operation(
            summary = "Создание категории контрагента",
            description = "Создает новую категорию для контрагентов. Требуется право COUNTERPARTY_WORK"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категория успешно создана"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные или категория уже существует"),
            @ApiResponse(responseCode = "401", description = "Недостаточно прав (требуется COUNTERPARTY_WORK)"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<?> createCounterpartyCategory(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания категории контрагента",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CounterpartyCategoryDTO.class))
            )
            @RequestBody CounterpartyCategoryDTO dto,
            HttpServletRequest request){
        try{
            if(!dto.isValid())
                return ResponseEntity.badRequest().body("invalid data");

//            if(request.getHeader("X-Authenticated") == null || request.getHeader("X-Authenticated").isEmpty())
//                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("request has been came not from gateway");

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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Logic error");
        }
    }

    @DeleteMapping("/delete-category")
    @Operation(
            summary = "Удаление категории контрагента",
            description = "Удаляет категорию контрагентов. Требуется право COUNTERPARTY_WORK"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категория успешно удалена"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные или категория не существует"),
            @ApiResponse(responseCode = "401", description = "Недостаточно прав (требуется COUNTERPARTY_WORK)"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<?> deleteCounterpartyCategory(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для удаления категории контрагента",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CounterpartyCategoryDTO.class))
            )
            @RequestBody CounterpartyCategoryDTO dto,
            HttpServletRequest request){
        try{
            if(!dto.isValid())
                return ResponseEntity.badRequest().body("invalid data");

//            if(request.getHeader("X-Authenticated") == null || request.getHeader("X-Authenticated").isEmpty())
//                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("request has been came not from gateway");

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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Logic error");
        }
    }
}