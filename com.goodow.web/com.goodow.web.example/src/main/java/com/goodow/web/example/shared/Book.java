package com.goodow.web.example.shared;

import com.goodow.web.core.shared.EntityType;
import com.goodow.web.security.shared.Content;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlType;

@XmlType
@Entity
@Table(name = "t_book")
public class Book extends Content {

  @Override
  public EntityType type() {
    return ExamplePackage.Book.as();
  }

}