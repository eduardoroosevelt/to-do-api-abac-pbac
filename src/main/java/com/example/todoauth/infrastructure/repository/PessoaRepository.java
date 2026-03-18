package com.example.todoauth.infrastructure.repository;

import com.example.todoauth.infrastructure.persistence.PessoaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PessoaRepository extends JpaRepository<PessoaEntity, Long> {
}
