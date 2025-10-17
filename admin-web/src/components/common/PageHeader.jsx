import React from 'react';
import { Typography } from 'antd';

const { Title, Text } = Typography;

/**
 * Reusable page header with optional icon and subtitle
 * Props:
 *  - icon: ReactNode (large icon / emoji)
 *  - title: string | ReactNode
 *  - subtitle: string | ReactNode (optional)
 *  - align: 'center' | 'left'
 */
const PageHeader = ({ icon, title, subtitle, align = 'center' }) => {
  return (
    <div className={`page-header page-header-${align}`}>
      {icon && <div className="page-header-icon">{icon}</div>}
      <Title level={2} className="page-header-title">{title}</Title>
      {subtitle && <Text className="page-header-subtitle">{subtitle}</Text>}
    </div>
  );
};

export default PageHeader;
