import React from 'react';
import Header from '../Header';
import Footer from '../Footer';
import { Outlet } from 'react-router-dom';
import { Layout, FloatButton } from 'antd';
import { ArrowUpOutlined } from '@ant-design/icons';

const { Content } = Layout;

const MainLayout = () => {
  return (
    <Layout 
      style={{ 
        minHeight: '100vh',
        width: '100%',
        maxWidth: '100vw',
        overflow: 'hidden'
      }}
      className="main-layout"
    >
      <Header />
      
      <Content 
        style={{ 
          paddingTop: window.innerWidth <= 768 ? '96px' : '64px',
          width: '100%',
          maxWidth: '100%',
          overflow: 'hidden'
        }}
      >
        <Outlet />
      </Content>
      
      <Footer />
      <FloatButton.BackTop visibilityHeight={160} shape="square" icon={<ArrowUpOutlined />} style={{ right: 24, bottom: 32, boxShadow:'0 10px 30px -8px rgba(0,0,0,.4),0 4px 14px -4px rgba(99,102,241,.5)', background:'linear-gradient(135deg,#6366f1,#8b5cf6)', color:'#fff', border:'none' }} />
    </Layout>
  );
};

export default MainLayout;