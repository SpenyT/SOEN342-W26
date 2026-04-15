package task;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RecurrencePattern {
    private final RecurrenceType type;
    private final int interval;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private List<DayOfWeek> selectedDays;
    private int dayOfMonth;

    public RecurrencePattern(RecurrenceType type, int interval, LocalDate startDate, LocalDate endDate) {
        this.type = type;
        this.interval = interval;
        this.startDate = startDate;
        this.endDate = endDate;
        this.selectedDays = new ArrayList<>();
        this.dayOfMonth = 1;
    }

    public List<LocalDate> generateOccurrenceDates() {
        switch (type) {
            case DAILY:   return generateDailyDates();
            case WEEKLY:  return generateWeeklyDates();
            case MONTHLY: return generateMonthlyDates();
            default:      return new ArrayList<>();
        }
    }

    private List<LocalDate> generateDailyDates() {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate d = startDate;
        while (!d.isAfter(endDate)) {
            dates.add(d);
            d = d.plusDays(interval);
        }
        return dates;
    }

    private List<LocalDate> generateWeeklyDates() {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate weekStart = startDate;
        while (!weekStart.isAfter(endDate)) {
            if (selectedDays.isEmpty()) {
                dates.add(weekStart);
            } else {
                for (DayOfWeek day : selectedDays) {
                    LocalDate candidate = weekStart.with(day);
                    if (!candidate.isBefore(startDate) && !candidate.isAfter(endDate)) {
                        dates.add(candidate);
                    }
                }
            }
            weekStart = weekStart.plusWeeks(interval);
        }
        return dates;
    }

    private List<LocalDate> generateMonthlyDates() {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate month = startDate;
        while (!month.isAfter(endDate)) {
            int clampedDay = Math.min(dayOfMonth, month.lengthOfMonth());
            LocalDate candidate = month.withDayOfMonth(clampedDay);
            if (!candidate.isBefore(startDate) && !candidate.isAfter(endDate)) {
                dates.add(candidate);
            }
            month = month.plusMonths(interval);
        }
        return dates;
    }

    public RecurrenceType getType() { return type; }
    public int getInterval() { return interval; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public List<DayOfWeek> getSelectedDays() { return selectedDays; }
    public void setSelectedDays(List<DayOfWeek> days) { this.selectedDays = days; }
    public int getDayOfMonth() { return dayOfMonth; }
    public void setDayOfMonth(int dayOfMonth) { this.dayOfMonth = dayOfMonth; }
}
