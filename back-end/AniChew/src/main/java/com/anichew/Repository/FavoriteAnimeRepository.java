package com.anichew.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.anichew.Entity.Anime;
import com.anichew.Entity.FavoriteAnime;
import com.anichew.Entity.User;

public interface FavoriteAnimeRepository  extends JpaRepository<FavoriteAnime,Long> {
	boolean existsByUserAndAnime(User user, Anime anime);
}
