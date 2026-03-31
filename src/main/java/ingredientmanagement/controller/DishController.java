package ingredientmanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ingredientmanagement.entity.Dish;
import ingredientmanagement.entity.DishIngredient;
import ingredientmanagement.entity.Ingredient;
import ingredientmanagement.service.DishService;

import java.util.List;

@RestController
public class DishController {

    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @GetMapping("/dishes")
    public ResponseEntity<List<Dish>> getDishes() {
        return ResponseEntity.ok(dishService.getDishes());
    }

    @GetMapping("/dishes/{id}")
    public ResponseEntity<?> getDishById(@PathVariable Integer id) {
        Dish dish = dishService.getDishById(id);
        if (dish == null) {
            return ResponseEntity.status(404).body("Dish.id=" + id + " is not found");
        }
        return ResponseEntity.ok(dish);
    }

    @PutMapping("/dishes/{id}/ingredients")
    public ResponseEntity<?> updateDishIngredients(
            @PathVariable Integer id,
            @RequestBody(required = false) List<Ingredient> ingredients) {

        if (ingredients == null) {
            return ResponseEntity.status(400)
                    .body("Request body is required and must contain a list of ingredients.");
        }

        Dish dish = dishService.getDishById(id);
        if (dish == null) {
            return ResponseEntity.status(404)
                    .body("Dish.id=" + id + " is not found");
        }

        Dish updated = dishService.updateDishIngredients(id, ingredients);
        return ResponseEntity.ok(updated);
    }
}
