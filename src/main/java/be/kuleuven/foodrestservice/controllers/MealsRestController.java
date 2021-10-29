package be.kuleuven.foodrestservice.controllers;

import be.kuleuven.foodrestservice.domain.Meal;
import be.kuleuven.foodrestservice.domain.MealsRepository;
import be.kuleuven.foodrestservice.exceptions.MealNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
public class MealsRestController {

    private final MealsRepository mealsRepository;

    @Autowired
    MealsRestController(MealsRepository mealsRepository) {
        this.mealsRepository = mealsRepository;
    }

    @GetMapping("/rest/meals/{id}")
    EntityModel<Meal> getMealById(@PathVariable String id) {
        Meal meal = mealsRepository.findMeal(id).orElseThrow(() -> new MealNotFoundException(id));

        return mealToEntityModel(id, meal);
    }

    @GetMapping("/rest/meals")
    CollectionModel<EntityModel<Meal>> getMeals() {
        Collection<Meal> meals = mealsRepository.getAllMeal();

        List<EntityModel<Meal>> mealEntityModels = new ArrayList<>();
        for (Meal m : meals) {
            EntityModel<Meal> em = mealToEntityModel(m.getId(), m);
            mealEntityModels.add(em);
        }
        return CollectionModel.of(mealEntityModels,
                linkTo(methodOn(MealsRestController.class).getMeals()).withSelfRel());
    }

    @GetMapping("/rest/meals/biggest")
    EntityModel<Meal> getBiggestMeal() {
        Collection<Meal> meals = mealsRepository.getAllMeal();
        Meal biggestMeal = null;
        for(Meal m : meals){
            if(biggestMeal == null || biggestMeal.getKcal()<m.getKcal()){
                biggestMeal = m;
            }
        }
        return mealToEntityModel(biggestMeal.getId(), biggestMeal);
    }

    @GetMapping("/rest/meals/cheapest")
    EntityModel<Meal> getCheapestMeal() {
        Collection<Meal> meals = mealsRepository.getAllMeal();
        Meal cheapestMeal = null;
        for(Meal m : meals){
            if(cheapestMeal == null || cheapestMeal.getPrice()>m.getPrice()){
                cheapestMeal = m;
            }
        }
        return mealToEntityModel(cheapestMeal.getId(), cheapestMeal);
    }

    @PostMapping("/rest/meals")
    ResponseEntity<?> addMeal(@RequestBody Meal newMeal){
        EntityModel<Meal> entityModel = mealToEntityModel(newMeal.getId(), newMeal);
        mealsRepository.saveMeal(newMeal);
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @PutMapping("rest/meals/{id}")
    ResponseEntity<?> updateMeal(@RequestBody Meal newMeal, @PathVariable String id){
        Meal updatedMeal = mealsRepository.findMeal(id).map(meal -> {
            meal.setName(newMeal.getName());
            meal.setKcal(newMeal.getKcal());
            meal.setMealType(newMeal.getMealType());
            meal.setDescription(newMeal.getDescription());
            meal.setPrice(newMeal.getPrice());
            return meal;
        }).orElseThrow(()->new MealNotFoundException(id));
        mealsRepository.saveMeal(updatedMeal);
        EntityModel<Meal> entityModel = mealToEntityModel(updatedMeal.getId(),updatedMeal);
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @DeleteMapping("rest/meals/{id}")
    ResponseEntity<?> deleteMeal(@PathVariable String id){
        Meal meal = mealsRepository.findMeal(id).orElseThrow(() -> new MealNotFoundException(id));
        mealsRepository.deleteMeal(meal.getId());
        return ResponseEntity.noContent().build();
    }

    private EntityModel<Meal> mealToEntityModel(String id, Meal meal) {
        return EntityModel.of(meal,
                linkTo(methodOn(MealsRestController.class).getMealById(id)).withSelfRel(),
                linkTo(methodOn(MealsRestController.class).getMeals()).withRel("rest/meals")
                );
    }
}
