
package com.exemple.historique.service;

import com.exemple.historique.model.Operation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OperationService {

    private final Map<Integer, Operation> operations =
            new ConcurrentHashMap<>();

    private final AtomicInteger compteur = new AtomicInteger(0);

    // Ajouter une opération
    public Operation ajouter(Operation op) {

        int id = compteur.incrementAndGet();

        op.setId(id);

        operations.put(id, op);

        return op;
    }

    // Retourner toutes les opérations
    public List<Operation> lister() {
        return new ArrayList<>(operations.values());
    }

    // Lire une opération par ID
    public Operation lire(int id) {
        return operations.get(id);
    }

    // Vérifier si une opération existe
    public boolean existe(int id) {
        return operations.containsKey(id);
    }

    // Remplacer une opération
    public void remplacer(int id, Operation op) {
        op.setId(id);
        operations.put(id, op);
    }

    // Supprimer une opération
    public Operation supprimer(int id) {
        return operations.remove(id);
    }
}
