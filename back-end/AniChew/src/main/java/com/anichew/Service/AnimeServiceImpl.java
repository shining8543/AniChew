package com.anichew.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.anichew.Entity.Anime;
import com.anichew.Entity.AnimeChara;
import com.anichew.Entity.AnimeGenre;
import com.anichew.Entity.AnimeReview;
import com.anichew.Entity.AnimeReviewLove;
import com.anichew.Entity.AnimeSeries;
import com.anichew.Entity.Animescore;
import com.anichew.Entity.Chara;
import com.anichew.Entity.FavoriteAnime;
import com.anichew.Entity.User;
import com.anichew.Repository.AnimeCharaRepository;
import com.anichew.Repository.AnimeGenreRepository;
import com.anichew.Repository.AnimeRepository;
import com.anichew.Repository.AnimeReviewLoveRepository;
import com.anichew.Repository.AnimeReviewRepository;
import com.anichew.Repository.AnimeSeriesRepository;
import com.anichew.Repository.AnimescoreRepository;
import com.anichew.Repository.FavoriteAnimeRepository;
import com.anichew.Repository.UserRepository;
import com.anichew.Request.ReviewRequest;
import com.anichew.Response.AnimeDetailResponse;
import com.anichew.Response.AnimeResponse;
import com.anichew.Response.CharaResponse;
import com.anichew.Response.GenreResponse;
import com.anichew.Response.ReviewResponse;
import com.anichew.Response.ScoreResponse;
import com.anichew.Response.SeriesResponse;
import com.anichew.Util.JwtUtil;

@Service
public class AnimeServiceImpl implements AnimeService {

	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private AnimeRepository animeRepo;
	
	@Autowired
	private AnimescoreRepository animerateRepo;
	
	@Autowired
	private AnimeGenreRepository animeGenreRepo;
	
	@Autowired
	private AnimeSeriesRepository animeSeriesRepo;
	
	@Autowired
	private AnimeCharaRepository animeCharaRepo;
	
	@Autowired
	private FavoriteAnimeRepository favoriteAnimeRepo;
	
	@Autowired
	private AnimescoreRepository animescoreRepo;
	
	@Autowired
	private AnimeReviewRepository animeReviewRepo;
	
	@Autowired
	private AnimeReviewLoveRepository animeReviewLoveRepo;	
	
	@Autowired
	private JwtUtil jwtUtil;
	
	@Override
	public ScoreResponse rateAnime(HttpServletRequest httpServletReq, long animeid, float score) {
		
		final String requestTokenHeader = httpServletReq.getHeader("Authorization");
		String userid = jwtUtil.getUserid(requestTokenHeader);
		User user = userRepo.findById(Long.parseLong(userid));		
		
		Anime anime = animeRepo.findById(animeid);
		
		Animescore animerate;
		
		if(animerateRepo.existsByUserAndAnime(user, anime)) {
			animerate = animerateRepo.findByUserAndAnime(user,anime);			
		}else {
			animerate = new Animescore(user, anime);
		}
		
		animerate.setScore(score);
		
		animerateRepo.save(animerate);
		
		ScoreResponse response = new ScoreResponse();
		response.setId(animeid);
		response.setUserId(Long.parseLong(userid));
		response.setType("ANIME");
		response.setScore(score);
		
		
		
		return response;
	}


	@Override
	public boolean deleteRate(HttpServletRequest httpServletReq, long animeid) {
		
		final String requestTokenHeader = httpServletReq.getHeader("Authorization");
		String userid = jwtUtil.getUserid(requestTokenHeader);
		User user = userRepo.findById(Long.parseLong(userid));		
		Anime anime = animeRepo.findById(animeid);
		
		animerateRepo.deleteByUserAndAnime(user, anime);
		
		
				
		return true;
	}

	@Override
	public boolean existsAnimerate(HttpServletRequest httpServletReq, long animeid) {
		
		final String requestTokenHeader = httpServletReq.getHeader("Authorization");
		String userid = jwtUtil.getUserid(requestTokenHeader);
		User user = userRepo.findById(Long.parseLong(userid));		
		
		Anime anime = animeRepo.findById(animeid);
				
		if(animerateRepo.existsByUserAndAnime(user, anime))
			return true;
		
		return false;
	}

	@Override
	public boolean existsAnime(long animeid) {
		
		if(animeRepo.existsById(animeid))
			return true;
		
		return false;
	}


	@Override
	public AnimeDetailResponse animeDetail(HttpServletRequest httpServletReq, long animeid) {
		
		final String requestTokenHeader = httpServletReq.getHeader("Authorization");
		boolean isFavorite = false;
		String accessor = null;
		if (requestTokenHeader != null) {
			accessor = jwtUtil.getUserid(requestTokenHeader);
		}		
	
		

		Anime anime = animeRepo.findById(animeid);
		AnimeDetailResponse response = new AnimeDetailResponse(anime);
		
		
		long accessor_id = -1;
		User user = null;
		if(accessor!=null) {
			accessor_id = Long.parseLong(accessor);
			 user = userRepo.findById(accessor_id);
			
			isFavorite = favoriteAnimeRepo.existsByUserAndAnime(user, anime);
		}
		
		
		
		List<AnimeGenre> animeGenres = animeGenreRepo.findAllByAnime(anime);
		
		List<GenreResponse> genres = new ArrayList();
		
		for(AnimeGenre aGenre : animeGenres) {
			GenreResponse genre = new GenreResponse();
			genre.setId(aGenre.getGenre().getId());
			genre.setName(aGenre.getGenre().getName());
			
			genres.add(genre);			
		}		
		
		List<AnimeResponse> relatedAnimes = new ArrayList();
		
		AnimeSeries seriesAnime = animeSeriesRepo.findByAnime(anime);
		List<AnimeSeries> seriesAnimes = null; 
		SeriesResponse series = new SeriesResponse();
		
		if(seriesAnime != null) {
			seriesAnimes = animeSeriesRepo.findAllBySeries(seriesAnime.getSeries());
			series.setId(seriesAnime.getSeries().getId());
			series.setName(seriesAnime.getSeries().getName());
			for(AnimeSeries aSeries : seriesAnimes) {
				AnimeResponse relatedAnime = new AnimeResponse();
				relatedAnime.setId(aSeries.getAnime().getId());
				relatedAnime.setKoreanName(aSeries.getAnime().getKoreanName());
				relatedAnime.setName(aSeries.getAnime().getName());			
				relatedAnimes.add(relatedAnime);
			}
			
		}
		
	
				
		float avgScore=0;
		
//		avgScore = animescoreRepo.avgByAnime((Long)anime.getId()); 
		
		long scores[] = new long[5];
		for(int i=1;i<=10;i++) {
			long cnt = animescoreRepo.countByAnimeAndScore(anime, i);
			scores[(i-1)/2] += cnt;
		}			
		
		float myScore = 0;
		
		if(user!=null && animescoreRepo.existsByUserAndAnime(user, anime)) {
			myScore = animescoreRepo.findByUserAndAnime(user, anime).getScore();
		}
		
		
		
		response.setGenres(genres);
		response.setSeries(series);
		response.setAvgScore(avgScore);
		response.setScores(scores);
		response.setRelatedAnimes(relatedAnimes);
		response.setFavorite(isFavorite);
		response.setMyScore(myScore);
		
		return response;
	}
	
	
	public boolean existsReview(HttpServletRequest httpServletReq, long animeid) {
		
		final String requestTokenHeader = httpServletReq.getHeader("Authorization");
		String userid = jwtUtil.getUserid(requestTokenHeader);
		User user = userRepo.findById(Long.parseLong(userid));			
		Anime anime = animeRepo.findById(animeid);
		
		return animeReviewRepo.existsByUserAndAnime(user, anime);
	}
	
	public boolean existsReview(long reviewid) {
		
		return animeReviewRepo.existsById(reviewid);		
		
	}
	
	
	public ReviewResponse getMyReview(HttpServletRequest httpServletReq, long animeid) {
		
		final String requestTokenHeader = httpServletReq.getHeader("Authorization");
		String userid = jwtUtil.getUserid(requestTokenHeader);
		User user = userRepo.findById(Long.parseLong(userid));	
		Anime anime = animeRepo.findById(animeid);
		
		AnimeReview review = animeReviewRepo.findByUserAndAnime(user, anime);
		
		ReviewResponse response = new ReviewResponse(review);
		response.setMine(true);
		response.setLoveCnt(review.getLoves().size());		
		if(animeReviewLoveRepo.existsByUserAndReview(user, review))
			response.setLove(true);
		
		return response;
	}
	
	
	public ReviewResponse writeReview(HttpServletRequest httpServletReq, String content, long animeid) {
		
		ReviewResponse response = null;
		
		
		final String requestTokenHeader = httpServletReq.getHeader("Authorization");
		String userid = jwtUtil.getUserid(requestTokenHeader);
		User user = userRepo.findById(Long.parseLong(userid));	
		
		Anime anime = animeRepo.findById(animeid);
		
		AnimeReview review = AnimeReview.builder()
				.content(content)
				.user(user)
				.anime(anime)
				.createdDate(LocalDateTime.now())
				.modifiedDate(LocalDateTime.now())
				.build();
		
		animeReviewRepo.save(review);
		
				

		response = new ReviewResponse(review);
		response.setMine(true);
		
		review = animeReviewRepo.findByUserAndAnime(user, anime);
		response.setReviewId(review.getId());
		
		return response;
				
	}
	
	public ReviewResponse modifyReview(HttpServletRequest httpServletReq, ReviewRequest req, long anime_id) {
		
		ReviewResponse response = null;
		
		AnimeReview review = null;
		
		if(!animeReviewRepo.existsById(req.getId())) 
			return null;
		
		review = animeReviewRepo.findById(req.getId());
		
		final String requestTokenHeader = httpServletReq.getHeader("Authorization");
		String userid = jwtUtil.getUserid(requestTokenHeader);
		
		User user = userRepo.findById(Long.parseLong(userid));			
		Anime anime = animeRepo.findById(anime_id);
		
		
		if(review.getUser() != user) 
			return null;
		
		
		
		review = AnimeReview.builder()
				.id(review.getId())
				.content(req.getContent())
				.user(user)
				.anime(anime)
				.createdDate(review.getCreatedDate())
				.modifiedDate(LocalDateTime.now())
				.build();
		
		
		
		animeReviewRepo.save(review);
		
				
		
		response = new ReviewResponse(review);
		if(animeReviewLoveRepo.existsByUserAndReview(user, review))
			response.setLove(true);
		response.setMine(true);
		
		
		return response;
				
	}
	
	public boolean deleteReview(HttpServletRequest httpServletReq, long reviewid, long anime_id) {
		
		ReviewResponse response = null;
		
		AnimeReview review = null;
		
		if(!animeReviewRepo.existsById(reviewid)) 
			return false;
		
		review = animeReviewRepo.findById(reviewid);
		
		final String requestTokenHeader = httpServletReq.getHeader("Authorization");
		String userid = jwtUtil.getUserid(requestTokenHeader);
		
		User user = userRepo.findById(Long.parseLong(userid));			
		Anime anime = animeRepo.findById(anime_id);
		
		
		if(review.getUser() != user) 
			return false;
		
		
		animeReviewRepo.delete(review);
		
				
		
		
		return true;
				
	}
	
	public boolean exsitsReviewLove(HttpServletRequest httpServletReq, long reviewid) {
		final String requestTokenHeader = httpServletReq.getHeader("Authorization");
		String userid = jwtUtil.getUserid(requestTokenHeader);		
		User user = userRepo.findById(Long.parseLong(userid));	
		AnimeReview review = animeReviewRepo.findById(reviewid);
		
		
		
		
		return animeReviewLoveRepo.existsByUserAndReview(user, review);
	}
	
	public void reviewLove(HttpServletRequest httpServletReq, long reviewid) {
		final String requestTokenHeader = httpServletReq.getHeader("Authorization");
		String userid = jwtUtil.getUserid(requestTokenHeader);		
		User user = userRepo.findById(Long.parseLong(userid));	
		AnimeReview review = animeReviewRepo.findById(reviewid);
		
		AnimeReviewLove reviewLove = AnimeReviewLove.builder().user(user).review(review).build();
		
		animeReviewLoveRepo.save(reviewLove);
		
		
	}
	

	public boolean deleteReviewLove(HttpServletRequest httpServletReq, long reviewid) {
		final String requestTokenHeader = httpServletReq.getHeader("Authorization");
		String userid = jwtUtil.getUserid(requestTokenHeader);		
		User user = userRepo.findById(Long.parseLong(userid));	
		AnimeReview review = animeReviewRepo.findById(reviewid);		
		
		animeReviewLoveRepo.deleteByUserAndReview(user,review);
		
		return true;
	}


	public boolean setFavoriteAnime(HttpServletRequest httpServletReq, long animeid) {		
		final String requestTokenHeader = httpServletReq.getHeader("Authorization");
		String userid = jwtUtil.getUserid(requestTokenHeader);		
		User user = userRepo.findById(Long.parseLong(userid));	
		
		Anime anime = animeRepo.findById(animeid);
		
		if(favoriteAnimeRepo.existsByUserAndAnime(user, anime))
			return false;
		
		
		FavoriteAnime favoriteAnime = new FavoriteAnime(user,anime);
		favoriteAnimeRepo.save(favoriteAnime);
		
		
		
		return true;
	}
	
	public boolean deleteFavoriteAnime(HttpServletRequest httpServletReq, long animeid) {		
		final String requestTokenHeader = httpServletReq.getHeader("Authorization");
		String userid = jwtUtil.getUserid(requestTokenHeader);		
		User user = userRepo.findById(Long.parseLong(userid));	
		
		Anime anime = animeRepo.findById(animeid);
		
		if(!favoriteAnimeRepo.existsByUserAndAnime(user, anime))
			return false;
		
		
		
		favoriteAnimeRepo.deleteByUserAndAnime(user,anime);
		
		
		
		return true;
	}


	@Override
	public List<ReviewResponse> getReviews(HttpServletRequest httpServletReq, long animeid) {
		
		
		final String requestTokenHeader = httpServletReq.getHeader("Authorization");
		boolean isFavorite = false;
		String accessor = null;
		if (requestTokenHeader != null) {
			accessor = jwtUtil.getUserid(requestTokenHeader);
		}		
	
		

		Anime anime = animeRepo.findById(animeid);
		AnimeDetailResponse response = new AnimeDetailResponse(anime);
		
		
		long accessor_id = -1;
		User user = null;
		if(accessor!=null) {
			accessor_id = Long.parseLong(accessor);
			 user = userRepo.findById(accessor_id);
		}
				
		
		List<AnimeReview> reviews = animeReviewRepo.findAllByAnime(anime);
		
		List<ReviewResponse> reviewsRes = new ArrayList();
		for(AnimeReview review : reviews) {
			ReviewResponse reviewRes = new ReviewResponse(review);
			
			if(review.getUser().getId() == accessor_id)
				reviewRes.setMine(true);
			
			
			reviewRes.setLoveCnt(review.getLoves().size());
			if(user !=null && animeReviewLoveRepo.existsByUserAndReview(user, review))
				reviewRes.setLove(true);
			
			reviewRes.setLoveCnt(review.getLoves().size());
			reviewsRes.add(reviewRes);
		}
		
		
		
		
		
		return reviewsRes;
	}


	public List<CharaResponse> getCharas(long animeid){
		
		
		Anime anime = animeRepo.findById(animeid);
		
		List<AnimeChara> animeCharas = animeCharaRepo.findAllByAnime(anime);
		List<CharaResponse> response = new ArrayList();
		
		for(AnimeChara animeChara : animeCharas) {
			Chara chara = animeChara.getChara();
			CharaResponse charaRes = new CharaResponse();
			charaRes.setId(chara.getId());
			charaRes.setFirstName(chara.getFirstName());
			charaRes.setLastName(chara.getLastName());
			
			
			
			response.add(charaRes);			
			
		}
		
		
		return response;
	}
	

	
	

}
