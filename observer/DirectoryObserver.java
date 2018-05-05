package com.directory.observer;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.directory.observer.File.computeMd5Hash;

public class DirectoryObserver {
    private final static String DEFAULT_RESULTS_FILENAME = "D:\\Temp\\DirectoryObserver\\observeResults.txt";

    public static void main(String[] args) throws IOException {
        //проверяем, что в параметрах указана директория для наблюдения
        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("Path to directory not found");
        }

        //задаем параметры
        String observePath = args[0];
        String resultsFilename;
        //если не указан второй параметр (файл, в который сохранять результаты после проверки)
        if (args.length > 1) {              //если указан
            resultsFilename = args[1];
        } else {                            //иначе задаем по умолчанию
            resultsFilename = DEFAULT_RESULTS_FILENAME;
        }

        //данные для текущей обработки каталога и обработки на предыдущем запуске
        Set<ObservableObject> data = new HashSet<>();
        Set<ObservableObject> previousData = loadData(resultsFilename);
        observe(Paths.get(observePath), data);

        //ищем отличия и выводим их
        Map<String, PathObjectStatus> map = compute(data, previousData);
        for (Map.Entry<String, PathObjectStatus> entry : map.entrySet()) {
            System.out.println(entry.getKey() + "\t\t" + entry.getValue());
        }

        //сохраняем результаты текущего просмотра каталога
        saveData(resultsFilename, (HashSet<ObservableObject>) data);
    }

    //поиск различий между запусками
    private static Map<String, PathObjectStatus> compute(Set<ObservableObject> current, Set<ObservableObject> previous) {
        HashMap<String, PathObjectStatus> resultMap = new HashMap<>();
        if (previous == null || previous.isEmpty()) {
            return resultMap;
        }
        current = new HashSet<>(current);
        previous = new HashSet<>(previous);

        removeCommonItems(current, previous);
        findDeletedModifiedMoved(current, previous, resultMap);

        for (ObservableObject observableObject : current) {
            resultMap.put(observableObject.getPath(), PathObjectStatus.CREATED);
        }

        return resultMap;
    }

    //удаляем одинаковые элементы из двух множеств
    private static void removeCommonItems(Set<ObservableObject> current, Set<ObservableObject> previous) {
        Set<ObservableObject> toRemove = current.stream()
                .filter(previous::contains)
                .collect(Collectors.toSet());

        current.removeAll(toRemove);
        previous.removeAll(toRemove);
        toRemove.clear();
    }

    //добавляем в map удаленные, измененные и перемещенные элементы. множества при этом изменяются
    private static void findDeletedModifiedMoved(Set<ObservableObject> current, Set<ObservableObject> previous, HashMap<String, PathObjectStatus> map) {
        for (ObservableObject observableObject : previous) {
            map.put(observableObject.getPath(), PathObjectStatus.DELETED);
            Iterator<ObservableObject> iterator = current.iterator();
            while (iterator.hasNext()) {
                ObservableObject currentObj = iterator.next();
                if (observableObject.isModify(currentObj)) {
                    map.put(observableObject.getPath(), PathObjectStatus.MODIFIED);
                    iterator.remove();
                    break;
                } else if (observableObject.isMove(currentObj)) {
                    map.put(observableObject.getPath(), PathObjectStatus.MOVED);
                    iterator.remove();
                    break;
                }
            }
        }
    }

    //для перевода даты создания файла из FileTime в LocalDateTime
    private static LocalDateTime convertDate(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    //"сбор" информации о каталоге для наблюдения
    private static void observe(Path path, Set<ObservableObject> set) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (path.equals(dir)) {
                        return FileVisitResult.CONTINUE;
                    }
                    Directory directory = new Directory();
                    directory.setPath(dir.toAbsolutePath().toString());
                    directory.setCreationTime(convertDate(attrs.creationTime().toInstant()));
                    directory.setLastModifiedTime(convertDate(attrs.lastModifiedTime().toInstant()));
                    set.add(directory);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    File fileObject = new File();
                    fileObject.setPath(file.toAbsolutePath().toString());
                    fileObject.setCreationTime(convertDate(attrs.creationTime().toInstant()));
                    fileObject.setLastModifiedTime(convertDate(attrs.lastModifiedTime().toInstant()));
                    fileObject.setMd5Hash(computeMd5Hash(file));
                    set.add(fileObject);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //загрузка данных из файла
    private static Set<ObservableObject> loadData(String filename) {
        if (Files.notExists(Paths.get(filename))) {
            return null;
        }
        HashSet<ObservableObject> set = null;

        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            set = (HashSet<ObservableObject>) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return set;
    }

    //сохранение результатов в файл
    private static void saveData(String filename, HashSet<ObservableObject> set) throws IOException {
        Path path = Paths.get(filename);
        Files.createDirectories(path.getParent());
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(set);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
