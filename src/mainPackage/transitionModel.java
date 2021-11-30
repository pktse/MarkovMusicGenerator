package mainPackage;

import java.util.*;
import java.util.stream.Collectors;

public class transitionModel {
    public HashMap<List<Long>, HashMap<Long, Long>> transModel = new HashMap<>(new HashMap<>());

    public ArrayList<Long> possiblePredictions = new ArrayList<>();

    public void addToPossible(Long key) {
        possiblePredictions.add(key);
    }

    public void addToTransModel(Long key, List<Long> prev) {
        addToPossible(key);
        if (prev.size() < 1) {
            return;
        }
        HashMap<Long, Long> notesHistory = transModel.get(prev);
        if (notesHistory == null) {
            notesHistory = new HashMap<>();
        }
        notesHistory.put(key, notesHistory.getOrDefault(prev, 0L) + 1);
        transModel.put(prev, notesHistory);
        if (prev.size() > 1) {
            addToTransModel(key, prev.subList(1, prev.size()
            ));
        }
    }

    public long getPrediction(ArrayList<Long> prev) {
        possiblePredictions = new ArrayList<> (possiblePredictions.stream()
                .distinct()
                .collect(Collectors.toList()));
        if (prev.size() == 0) {
            return possiblePredictions.get((int) Math.floor(Math.random() * possiblePredictions.size()));
        }
        while (prev.size() >= 1) {
            double chosen = Math.random();
            double total = 0L;
            if (transModel.get(prev) == null) {
                return possiblePredictions.get((int) Math.floor(Math.random() * possiblePredictions.size()));
            } else {
                for (Long value : transModel.get(prev).values()) {
                    total += value;
                }
                double curr = 0L;
                for (Map.Entry<Long, Long> entry : transModel.get(prev).entrySet()) {
                    curr += entry.getValue() / total;
                    if (chosen <= curr) {
                        return entry.getKey();
                    }
                }
            }
            if (prev.size() > 1) {
                prev = new ArrayList<>(prev.subList(1, prev.size() - 1));
            } else {
                break;
            }
        }
        return possiblePredictions.get((int) Math.floor(Math.random() * possiblePredictions.size()));
    }

    @Override
    public String toString() {
        String returnString = "";

        List<List<Long>> keys = new LinkedList<List<Long>>(transModel.keySet());
        Collections.sort(keys, new Comparator<List<Long>>() {
            @Override
            public int compare(List<Long> o1, List<Long> o2) {
                return o1.size() - o2.size();
            }
        });

        LinkedHashMap<List<Long>, HashMap<Long,Long>> sortedMap = new LinkedHashMap<>();
        for (List<Long> key: keys){
            sortedMap.put(key, transModel.get(key));
        }

        for (Map.Entry<List<Long>, HashMap<Long,Long>> entry: sortedMap.entrySet()) {
            returnString += "\n" + entry.getKey() + ": ";
            int total = 0;
            for (Long count : entry.getValue().values()) {
                total += count;
            }
            for (Map.Entry<Long, Long> entry2 : entry.getValue().entrySet()) {
                returnString += "\t " + entry2.getKey() + ": " + (float) entry2.getValue()/ (float) total + "";
            }
        }
        return returnString;
    }
}
