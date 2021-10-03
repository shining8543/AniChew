import React, { useEffect } from 'react';
import { observer } from 'mobx-react';
import { useParams } from 'react-router-dom';
import { useAni, useReview } from '../../hooks';
import HeaderSection from './components/header-section/header-section';
import NotFound from '../error/not-found';
import MainSection from './components/main-section/main-section';

const AniDetail = observer(() => {
  const param = useParams<{id: string}>();
  const ani = useAni();
  const review = useReview();

  useEffect(() => {
    ani.getAniDetailInfo(param.id);
    review.getAllReviews(param.id);
    review.getMyReview(param.id);
    window.scroll(0, 0);
  }, [param.id, ani, review]);

  return (
    <section>
      {ani.aniInfo ? (
        <>
          <HeaderSection info={ani.aniInfo} />
          <MainSection info={ani.aniInfo} />
        </>
      ) : <NotFound type="애니메이션 정보" />}
    </section>
  );
});

export default AniDetail;
