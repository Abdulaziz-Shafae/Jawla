package com.jawla.repo;
import com.jawla.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MemberRepository extends JpaRepository<Member,Long> {}