package com.example.couriermanagement.controller;

import com.example.couriermanagement.constants.BusinessRulesConstants;
import com.example.couriermanagement.dto.DeliveryDto;
import com.example.couriermanagement.dto.request.DeliveryRequest;
import com.example.couriermanagement.dto.request.DeliverySearchCriteria;
import com.example.couriermanagement.dto.request.GenerateDeliveriesRequest;
import com.example.couriermanagement.dto.response.GenerateDeliveriesResponse;
import com.example.couriermanagement.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deliveries")
@Tag(name = "Deliveries", description = "Управление доставками (менеджер)")
@SecurityRequirement(name = "bearerAuth")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
        summary = "Автоматически сгенерировать доставки на несколько дней",
        description = """
            Доступно только для менеджера. Система автоматически распределяет курьеров и машины.
            Запрос представляет собой мапу, где ключ - дата доставки, значение - список маршрутов с товарами.
        """
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Доставки успешно сгенерированы"),
            @ApiResponse(responseCode = "400", description = "Ошибка при генерации"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещено")
        }
    )
    public ResponseEntity<GenerateDeliveriesResponse> generateDeliveries(
        @Valid @RequestBody GenerateDeliveriesRequest generateRequest
    ) {
        GenerateDeliveriesResponse response = deliveryService.generateDeliveries(generateRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
        summary = "Поиск доставок с фильтрацией",
        description = """
            Поиск и фильтрация доставок по различным критериям. 
            Доступно только для менеджеру.
            
            Критерии фильтрации:
            - deliveryDate: дата доставки (опционально)
            - courierId: идентификатор курьера (опционально)
            - status: статус доставки (опционально)
            
            Пример: /deliveries?deliveryDate=2025-01-30&courierId=1&status=IN_PROGRESS
            """
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Список найденных доставок"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации критериев поиска"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещено")
        }
    )
    public ResponseEntity<List<DeliveryDto>> searchDeliveries(
        @Valid DeliverySearchCriteria criteria
    ) {
        List<DeliveryDto> deliveries = deliveryService.searchDeliveries(criteria);
        return ResponseEntity.ok(deliveries);
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
        summary = "Создать доставку вручную",
        description = "Доступно только для менеджера. Проверяется время маршрута и вместимость машины"
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201", description = "Доставка создана"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещено")
        }
    )
    public ResponseEntity<DeliveryDto> createDelivery(
        @Valid @RequestBody DeliveryRequest deliveryRequest
    ) {
        DeliveryDto delivery = deliveryService.createDelivery(deliveryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(delivery);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Получить детали доставки",
        description = "Получение подробной информации о доставке"
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Детали доставки"),
            @ApiResponse(responseCode = "404", description = "Доставка не найдена")
        }
    )
    public ResponseEntity<DeliveryDto> getDeliveryById(
        @Parameter(description = "ID доставки", example = "1")
        @PathVariable Long id
    ) {
        DeliveryDto delivery = deliveryService.getDeliveryById(id);
        return ResponseEntity.ok(delivery);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
        summary = "Обновить доставку",
        description = """
            Доступно только для менеджеру. 
            """ + BusinessRulesConstants.DELIVERY_EDIT_RULE_DESCRIPTION
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Доставка обновлена"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации (время/вместимость)"),
            @ApiResponse(responseCode = "403", description = "Редактирование запрещено"),
            @ApiResponse(responseCode = "404", description = "Доставка не найдена")
        }
    )
    public ResponseEntity<DeliveryDto> updateDelivery(
        @Parameter(description = "ID доставки", example = "1")
        @PathVariable Long id,
        @Valid @RequestBody DeliveryRequest deliveryRequest
    ) {
        DeliveryDto delivery = deliveryService.updateDelivery(id, deliveryRequest);
        return ResponseEntity.ok(delivery);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
        summary = "Удалить доставку",
        description = """
            Доступно только для менеджеру. 
            """ + BusinessRulesConstants.DELIVERY_EDIT_RULE_DESCRIPTION
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204", description = "Доставка удалена"),
            @ApiResponse(responseCode = "403", description = "Удаление запрещено (срок или роль)"),
            @ApiResponse(responseCode = "404", description = "Доставка не найдена")
        }
    )
    public ResponseEntity<Void> deleteDelivery(
        @Parameter(description = "ID доставки", example = "1")
        @PathVariable Long id
    ) {
        deliveryService.deleteDelivery(id);
        return ResponseEntity.noContent().build();
    }
}
