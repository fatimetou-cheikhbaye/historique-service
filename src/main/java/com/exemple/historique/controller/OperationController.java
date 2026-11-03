
package com.exemple.historique.controller;

import com.exemple.historique.client.CalculClient;
import com.exemple.historique.model.Operation;
import com.exemple.historique.service.OperationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/operations")
public class OperationController {

    private static final List<String> OPERATEURS =
            List.of("somme", "soustraction", "produit", "division");

    private final OperationService operationService;
    private final CalculClient calculClient;

    // Constructeur
    public OperationController(OperationService operationService,
                               CalculClient calculClient) {

        this.operationService = operationService;
        this.calculClient = calculClient;
    }

    // Demander le calcul au calcul-service
    private ResponseEntity<Object> calculer(Operation op) {

        // Vérifier l'opérateur
        if (op.getOperateur() == null ||
                !OPERATEURS.contains(op.getOperateur())) {

            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "Operateur invalide"));
        }

        // Interdire la division par zéro
        if ("division".equals(op.getOperateur()) && op.getB() == 0) {

            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "Division par zero impossible"));
        }

        try {

            double resultat = calculClient.calculer(
                    op.getOperateur(),
                    op.getA(),
                    op.getB()
            );

            op.setResultat(resultat);

            return null;

        } catch (HttpClientErrorException e) {

            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("erreur", "Erreur de calcul-service"));

        } catch (ResourceAccessException e) {

            return ResponseEntity.status(503)
                    .body(Map.of("erreur", "Calcul-service indisponible"));
        }
    }

    // POST /operations : créer une opération
    @PostMapping
    public ResponseEntity<Object> creer(@RequestBody Operation op) {

        ResponseEntity<Object> erreur = calculer(op);

        if (erreur != null) {
            return erreur;
        }

        Operation operation = operationService.ajouter(op);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(operation.getId())
                .toUri();

        return ResponseEntity.created(location).body(operation);
    }

    // GET /operations : retourner toutes les opérations
    @GetMapping
    public List<Operation> lister() {

        return operationService.lister();
    }

    // GET /operations/{id} : retourner une opération
    @GetMapping("/{id}")
    public ResponseEntity<Operation> lire(@PathVariable("id") int id) {

        Operation op = operationService.lire(id);

        if (op == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(op);
    }
}
