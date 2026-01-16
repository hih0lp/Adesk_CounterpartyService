package Adesk_CounterpartyService.Controllers;

import Adesk_CounterpartyService.Models.Category.CounterpartyCategoryDTO;
import Adesk_CounterpartyService.Models.Category.CounterpartyCategoryModel;
import Adesk_CounterpartyService.Models.Counterparty.CounterpartyModel;
import Adesk_CounterpartyService.Models.Counterparty.CounterpartyModelDTO;
import Adesk_CounterpartyService.Repositories.CounterpartyCategoriesRepository;
import Adesk_CounterpartyService.Repositories.CounterpartyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/counterparty/models")
@RequiredArgsConstructor
@Tag(name = "Контрагенты", description = "API для управления контрагентами")
@SecurityRequirement(name = "bearerAuth")
public class CounterpartyController {
    private final CounterpartyCategoriesRepository _counterpartyCategoriesRepository;
    private final CounterpartyRepository _counterpartyRepository;
    private final Logger log = LoggerFactory.getLogger(CounterpartyController.class);

    @PostMapping("/create-counterparty")
    @Operation(
            summary = "Создание контрагента",
            description = "Создает нового контрагента. Требуется право COUNTERPARTY_WORK"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Контрагент успешно создан"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные, категория не существует или контрагент уже существует"),
            @ApiResponse(responseCode = "401", description = "Недостаточно прав (требуется COUNTERPARTY_WORK)"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<?> createCounterparty(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания контрагента",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CounterpartyModelDTO.class))
            )
            @RequestBody CounterpartyModelDTO dto,
            HttpServletRequest request){
        try{
            if(!dto.isValid())
                return ResponseEntity.badRequest().body("invalid data");

//            if(request.getHeader("X-Authenticated") == null || request.getHeader("X-Authenticated").isEmpty())
//                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("request has been came not from gateway");

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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Logic error");
        }
    }


    /// СКОРЕЕ ВСЕГО, УДАЛЕНИЕ ДОЛЖНО БЫТЬ ПО АЙДИ КОМПАНИИ И ИМЕНИ
    @DeleteMapping("/delete-counterparty/{name}")
    @Operation(
            summary = "Удаление контрагента",
            description = "Удаляет контрагента по имени. Требуется право COUNTERPARTY_WORK. Примечание: Удаление происходит по имени контрагента и ID компании"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Контрагент успешно удален"),
            @ApiResponse(responseCode = "400", description = "Невалидное имя или контрагент не существует"),
            @ApiResponse(responseCode = "401", description = "Недостаточно прав (требуется COUNTERPARTY_WORK)"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<?> deleteCounterpartyCategory(
            @Parameter(
                    description = "Имя контрагента для удаления",
                    required = true,
                    example = "ООО 'Ромашка'"
            )
            @PathVariable String name,
            HttpServletRequest request){
        try{
            if(name.isEmpty())
                return ResponseEntity.badRequest().body("invalid data");

//            if(request.getHeader("X-Authenticated") == null || request.getHeader("X-Authenticated").isEmpty())
//                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("request has been came not from gateway");

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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Logic error");
        }
    }

    @GetMapping("/get-company-counterparties")
    @Operation(
            summary = "Получение контрагентов компании",
            description = "Возвращает список всех контрагентов текущей компании"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список контрагентов успешно получен"),
            @ApiResponse(responseCode = "204", description = "Контрагенты не найдены"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<?> getCompanyCounterparties(HttpServletRequest request){
        try{
            var counterparties = _counterpartyRepository.findByCompanyId(Long.parseLong(request.getHeader("X-Company-Id")));
            if(counterparties.isEmpty())
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

            return ResponseEntity.ok().body(counterparties);
        } catch(Exception ex) {
            log.error(ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Logic error");
        }
    }

    @GetMapping("/get-counterparty/{id}")
    @Operation(
            summary = "Получение контрагента по ID",
            description = "Возвращает информацию о контрагенте по его ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Контрагент найден"),
            @ApiResponse(responseCode = "404", description = "Контрагент не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<?> getCounterpartyById(
            @Parameter(
                    description = "ID контрагента",
                    required = true,
                    example = "123"
            )
            @PathVariable Long id){
        try{
            var counterpartyOpt = _counterpartyRepository.findById(id);
            if(counterpartyOpt.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Counterparty not found");

            return ResponseEntity.ok().body(counterpartyOpt.get());
        } catch(Exception ex) {
            log.error(ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Logic error");
        }
    }
}