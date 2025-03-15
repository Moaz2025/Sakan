package com.sakan.property;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactUsFormsService {
    @Autowired
    private ContactUsFormsRepository contactUsFormsRepository;

    public Form addForm(Form form) { return contactUsFormsRepository.save(form); }

    public Form editForm(Form form) { return contactUsFormsRepository.save(form); }

    public void deleteForm(Form form) { contactUsFormsRepository.delete(form); }

    public Form getFormById(int id) {
        return contactUsFormsRepository.findById(id).orElse(null);
    }

    public List<Form> getAllForms() {
        return contactUsFormsRepository.findAll();
    }

    public Page<Form> getForms(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return contactUsFormsRepository.findAll(pageable);
    }
}
