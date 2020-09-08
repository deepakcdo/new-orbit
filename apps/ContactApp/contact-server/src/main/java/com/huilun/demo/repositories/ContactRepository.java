package com.huilun.demo.repositories;

import com.huilun.demo.models.Contact;
import org.springframework.data.repository.CrudRepository;

public interface ContactRepository extends CrudRepository<Contact, String> {
    @Override
    void delete(Contact deleted);
}
