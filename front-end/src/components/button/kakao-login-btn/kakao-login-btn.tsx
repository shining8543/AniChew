import React from 'react';
import { Button } from 'antd';
import Text from 'antd/lib/typography/Text';

const KakaoLoginBtn = () => {
  const handleKakaoLogin = () => {
    window.location.href = `${process.env.REACT_APP_KAKAO_AUTH_URL}`;
  };

  return (
    <Button
      onClick={handleKakaoLogin}
      shape="round"
      size="large"
      style={styles.btn}
    >
      <img
        src="/assets/kakao-logo.jpg"
        alt="kakao-logo"
        width="27px"
        height="27px"
      />
      <Text style={styles.loginTitle}>Kakao</Text>
    </Button>
  );
};

const styles: CssKeyObject = {
  btn: {
    backgroundColor: '#fae300',
    display: 'flex',
    alignItems: 'center',
  },
  loginTitle: {
    marginLeft: '0.2em',
    fontWeight: 'bold',
  },
};

export default KakaoLoginBtn;
