package service;

import model.Task;
import model.Epic;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//==========================================================================================================
// Валидация задачи: недопустимость пересечения с другими задачами во времени.
// Для валидации используется создание набора чанков - периодов времени, продолжительность которых определена
// константой TIMECHUNK_DURATION (в минутах). Чанк описывается переменной, указывающей на начало времени чанка.
// Период, определяемый TIMECHUNK_DURATION, должен делить час без остатка (1-6, 10, 12 и т.д.)
//-----------------------------------------------------------------------------------------------------------

public class TimeIntersectionsValidator implements TaskValidator {
    final private Duration TIMECHUNK_DURATION = Duration.ofMinutes(10); //Продолжительность чанка
    final private HashMap<LocalDateTime,Task> timeChunks = new HashMap<>(); //Хранилище чанков
    final private HashMap<Integer,List<LocalDateTime>> timeChunksByTaskId = new HashMap<>(); //Храним чанки конкретных задач

    //Рассчитываем перечень чанков, относящихся к задаче
    private List<LocalDateTime> calculateChunksForTask (Task task) {
        List<LocalDateTime> chunks = new ArrayList<>();
        LocalDateTime chunk = calculateChunkForMoment(task.getStartTime());
        LocalDateTime endTime = task.getStartTime();

        if (task.getDuration()!=null) {
            endTime = task.getEndTime();
        }

        while (chunk.isBefore(endTime)) {
            chunks.add(chunk);
            chunk = chunk.plus(TIMECHUNK_DURATION);
        }
        return chunks;
    }

    private void removeTaskFromValidator (Task task) {
        if (task.getClass()==Epic.class) {
            return; //Временные параметры эпика расчитываюься от сабтасков, поэтому его не обрабатываем
        }
        if (timeChunksByTaskId.containsKey(task.getId())) {
            List<LocalDateTime> chunks = timeChunksByTaskId.get(task.getId());
            for (LocalDateTime chunk : chunks) {
                timeChunks.remove(chunk);
            }
            timeChunksByTaskId.remove(task.getId());
        }
    }

    private void addTaskToValidator (Task task) {
        if (task.getClass()==Epic.class) {
            return; //Временные параметры эпика расчитываюься от сабтасков, поэтому его не обрабатываем
        }
        if (task.getStartTime()==null) {
            return; //Если время не задано, нечего добавлять, задача не занимает чанков
        }
        //Удаляем ранее внесенные сведения за чанках задачи (при обновлении могут измениться)
        removeTaskFromValidator(task);

        //Составляем список чанков задачи
        List<LocalDateTime> chunks = calculateChunksForTask(task);

        //Занимаем чанки
        for (LocalDateTime chunk : chunks) {
            timeChunks.put(chunk,task);
        }

        //Привязываем чанки к идентификатору задачи
        timeChunksByTaskId.put(task.getId(),chunks);
    }

    @Override
    public void onAddTask(Task task) {
        addTaskToValidator(task);
    }

    @Override
    public void onRemoveTask (Task task) {
        removeTaskFromValidator(task);
    }

    @Override
    public void onRemoveTasks (List<Task> tasks) {
        for (Task task : tasks) {
            removeTaskFromValidator(task);
        }
    }

    @Override
    public void validate (TaskManager manager, Task task) {
        if (task.getStartTime()==null) {
            return; //задачи без времени валидны по умолчанию
        }

        if (task.getClass()==Epic.class) {
            return; //Временные параметры эпика расчитываюься от сабтасков, поэтому его не обрабатываем
        }

        if (task.getStartTime()==null) {
            return; //Если время не задано, задача валидна т.к. не имеет пересечений
        }

        //Составляем список чанков задачи
        List<LocalDateTime> chunks = calculateChunksForTask(task);

        //Проверяем наличие пересечений чанков с уже занятыми
        for (LocalDateTime chunk : chunks) {
            if (timeChunks.containsKey(chunk)) {
                throw new ManagerSaveException("Task "+task
                        + ") not valid /n,"
                        + "chunks ["+chunks+']'
                        +" has intersections with "
                        + timeChunks.get(chunk)+"/n"
                        + "chunks ["+calculateChunksForTask(timeChunks.get(chunk))+"]");
            }
        }
    }

    private LocalDateTime calculateChunkForMoment (LocalDateTime moment) {
        //Находим начало часа события
        LocalDateTime momentHour = moment.truncatedTo(ChronoUnit.HOURS);
        //Находим номер куска часа к которому относится событие исходя из заданных периодов кусков
        long hourChunk = Duration.between(momentHour,moment).dividedBy(TIMECHUNK_DURATION);
        return momentHour.plus(TIMECHUNK_DURATION.multipliedBy(hourChunk));
    }

}
