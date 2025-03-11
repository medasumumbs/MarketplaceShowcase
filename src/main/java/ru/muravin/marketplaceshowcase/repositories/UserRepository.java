package ru.muravin.marketplaceshowcase.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.muravin.marketplaceshowcase.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
