package com.scorpion.sleep;

/**
 * Created by stephen on 2015-11-29.
 */
public class HistoryItem {

    private int _historyId;
    private int _staffId;
    private int _customerId;
    private String _staffName;
    private String _customerName;
    private String _visitDate;
    private String _symptom;
    private String _resolution;
    private String _notes;

    public HistoryItem(int historyId, int staffId, int customerId, String staffName,
                       String customerName, String visitDate, String symptom,
                       String resolution, String notes) {
        _historyId = historyId;
        _staffId = staffId;
        _customerId = customerId;
        _staffName = staffName;
        _customerName = customerName;
        _visitDate = visitDate;
        _symptom = symptom;
        _resolution = resolution;
        _notes = notes;
    }

    public String getStaffName() {
        return _staffName;
    }

    public String getCustomerName() {
        return _customerName;
    }

    public String getVisitDate() {
        return _visitDate;
    }

    public String getSymptom() {
        return _symptom;
    }

    public String getResolution() {
        return _resolution;
    }

    public String getNotes() {
        return _notes;
    }
}
