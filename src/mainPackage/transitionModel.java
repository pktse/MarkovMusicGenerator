package mainPackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class transitionModel {
    public HashMap<List<Long>, HashMap<Long, Long>> transModel = new HashMap<>(new HashMap<>());
    public ArrayList<Long> possiblePredictions = new ArrayList<>();

    public void addToPossible(Long key) {
        possiblePredictions.add(key);
    }

    public void addToTransModel(Long key, List<Long> prev) {
        if (prev.size() <= 1) {
            return;
        }
        HashMap<Long, Long> notesHistory = transModel.get(prev);
        if (notesHistory == null) {
            notesHistory = new HashMap<>();
            addToPossible(key);
        }
        notesHistory.put(key, notesHistory.getOrDefault(prev, 0L) + 1);
        transModel.put(prev, notesHistory);
        addToTransModel(key, prev.subList(1, prev.size() - 1));
    }

    public long getPrediction(ArrayList<Long> prev) {
        while (prev.size() >= 1) {
            double chosen = Math.random();
            double total = 0L;
            if (transModel.get(prev) == null) {
                return possiblePredictions.get((int) Math.floor(Math.random() * possiblePredictions.size()));
            } else {
                for (Map.Entry<Long, Long> entry : transModel.get(prev).entrySet()) {
                    total += entry.getValue();
                }
                double curr = 0L;
                for (Map.Entry<Long, Long> entry : transModel.get(prev).entrySet()) {
                    curr += entry.getValue() / total;
                    if (chosen >= curr) {
                        return entry.getKey();
                    }
                }
            }
            prev = new ArrayList<>(prev.subList(1, prev.size() - 1));
        }
        return possiblePredictions.get((int) Math.floor(Math.random() * possiblePredictions.size()));
    }

    @Override
    public String toString() {
        String returnString = "";
        for (Map.Entry<List<Long>, HashMap<Long,Long>> entry: transModel.entrySet()) {
            returnString += "given: " + entry.getKey() + "\n";
            int total = 0;
            for (Long count : entry.getValue().values()) {
                total += count;
            }
            for (Map.Entry<Long, Long> entry2 : entry.getValue().entrySet()) {
                returnString += "\t " + entry2.getKey() + ": " + (float) entry2.getValue()/ (float) total + "\n";
            }
        }
        return returnString;
    }
}
