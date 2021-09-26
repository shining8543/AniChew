package com.anichew.Service;

import javax.servlet.http.HttpServletRequest;

import com.anichew.Response.AnimeDetailResponse;

public interface AnimeService {
	boolean rateAnime(HttpServletRequest httpServletReq, long animeid, float score);
	boolean deleteRate(HttpServletRequest httpServletReq, long animeid);
	boolean existsAnimerate(HttpServletRequest httpServletReq, long animeid);
	boolean exsitsAnime(HttpServletRequest httpServletReq, long animeid);
	AnimeDetailResponse animeDetail(HttpServletRequest httpServletReq, long animeid);
}
