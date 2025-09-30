import React, { useEffect } from 'react';
import { Route, Switch, withRouter } from 'react-router-dom';
import classnames from 'classnames';

import SettingsIcon from '@mui/icons-material/Settings';
import GithubIcon from '@mui/icons-material/GitHub';
import FacebookIcon from '@mui/icons-material/Facebook';
import TwitterIcon from '@mui/icons-material/Twitter';

import { Fab, IconButton } from '@mui/material';
import { connect } from 'react-redux';
// styles
import useStyles from './styles';

// components
import Header from '../Header';
import Sidebar from '../Sidebar';
import { Link } from '../Wrappers';
import ColorChangeThemePopper from './components/ColorChangeThemePopper';

import EditUser from '../../pages/user/EditUser';

// pages
import Dashboard from '../../pages/dashboard';
import TypographyPage from '../../pages/typography'
import ColorsPage from '../../pages/colors'
import GridPage from '../../pages/grid'

import StaticTablesPage from '../../pages/tables'
import DynamicTablesPage from '../../pages/tables/dynamic'

import IconsPage from '../../pages/icons'
import BadgesPage from '../../pages/badge'
import ModalsPage from '../../pages/modal'
import NotificationsPage from '../../pages/notifications'
import NavbarsPage from '../../pages/nav'
import TooltipsPage from '../../pages/tooltips'
import TabsPage from '../../pages/tabs'
import ProgressPage from '../../pages/progress'

import Ecommerce from '../../pages/ecommerce'
import Product from '../../pages/ecommerce/Products'
import ProductsGrid from '../../pages/ecommerce/ProductsGrid'
import CreateProduct from '../../pages/ecommerce/CreateProduct'

import FormsElements from '../../pages/forms/elements'
import FormValidation from '../../pages/forms/validation'

import Charts from '../../pages/charts'
import LineCharts from '../../pages/charts/LineCharts'
import BarCharts from '../../pages/charts/BarCharts'
import PieCharts from '../../pages/charts/PieCharts'

import Search from '../../pages/search'

import BreadCrumbs from '../../components/BreadCrumbs';

// context
import { useLayoutState } from '../../context/LayoutContext';
import { ProductsProvider } from '../../context/ProductContext'

import UsersFormPage from 'pages/CRUD/Users/form/UsersFormPage';
import UsersTablePage from 'pages/CRUD/Users/table/UsersTablePage';
import OrdersTablePage from 'pages/CRUD/Orders/table/OrdersTablePage';

//Sidebar structure
import structure from '../Sidebar/SidebarStructure'

const Redirect = (props) => {
  useEffect(() => window.location.replace(props.url));
  return <span>Redirecting...</span>;
};

function Layout(props) {
  const classes = useStyles();
  const [anchorEl, setAnchorEl] = React.useState(null);

  const open = Boolean(anchorEl);
  const id = open ? 'add-section-popover' : undefined;
  const handleClick = (event) => {
    setAnchorEl(open ? null : event.currentTarget);
  };

  // global
  let layoutState = useLayoutState();

  return (
    <div className={classes.root}>
      <Header history={props.history} />
      <Sidebar structure={structure}/>
      <div
        className={classnames(classes.content, {
          [classes.contentShift]: layoutState.isSidebarOpened,
        })}
      >
        <div className={classes.fakeToolbar} />
        <BreadCrumbs />
        <Switch>
          <Route path='/app/dashboard' component={Dashboard} />
          <Route path='/app/user/edit' component={EditUser} />

          <Route exact path="/app/core" render={() => <Redirect to="/app/core/typography" />} />
          <Route path="/app/core/typography" component={TypographyPage} />
          <Route path="/app/core/colors" component={ColorsPage} />
          <Route path="/app/core/grid" component={GridPage} />

          <Route exact path="/app/tables" render={() => <Redirect to={'/app/tables/static'} />} />
          <Route path="/app/tables/static" component={StaticTablesPage} />
          <Route path="/app/tables/dynamic" component={DynamicTablesPage} />

          <Route exact path="/app/ui" render={() => <Redirect to="/app/ui/icons" />} />
          <Route path="/app/ui/icons" component={IconsPage} />
          <Route path="/app/ui/badge" component={BadgesPage} />
          <Route path="/app/ui/modal" component={ModalsPage} />
          <Route path="/app/ui/navbar" component={NavbarsPage} />
          <Route path="/app/ui/tooltips" component={TooltipsPage} />
          <Route path="/app/ui/tabs" component={TabsPage} />
          <Route path="/app/ui/progress" component={ProgressPage} />
          <Route path="/app/ui/notifications" component={NotificationsPage} />

          <Route exact path="/app/forms" render={() => <Redirect to="/app/forms/elements" />} />
          <Route path="/app/forms/elements" component={FormsElements} />
          <Route path="/app/forms/validation" component={FormValidation} />

          <Route exact path="/app/charts" render={() => <Redirect to={'/app/charts/overview'} />} />
          <Route path="/app/charts/overview" component={Charts} />
          <Route path="/app/charts/line" component={LineCharts} />
          <Route path="/app/charts/bar" component={BarCharts} />
          <Route path="/app/charts/pie" component={PieCharts} />

          <Route path="/app/extra/search" component={Search} />

          <Route path="/app/ecommerce/management" exact>
            <ProductsProvider>
              <Ecommerce />
            </ProductsProvider>
          </Route>
          <Route path="/app/ecommerce/management/edit/:id" exact>
            <ProductsProvider>
              <CreateProduct />
            </ProductsProvider>
          </Route>
          <Route path="/app/ecommerce/management/create">
            <ProductsProvider>
              <CreateProduct />
            </ProductsProvider>
          </Route>
          <Route path="/app/ecommerce/product/:id" component={Product}/>
          <Route path="/app/ecommerce/product" component={Product} />
          <Route path="/app/ecommerce/gridproducts" component={ProductsGrid}/>

          <Route path={'/app/users'} exact component={UsersTablePage} />
          <Route path={'/app/user/new'} exact component={UsersFormPage} />
          <Route
            path={'/app/users/:id/edit'}
            exact
            component={UsersFormPage}
          />
          <Route path={'/app/orders'} exact component={OrdersTablePage} />
        </Switch>
        <Fab
          color='primary'
          aria-label='settings'
          onClick={(e) => handleClick(e)}
          className={classes.changeThemeFab}
          style={{ zIndex: 100 }}
        >
          <SettingsIcon style={{ color: '#fff' }} />
        </Fab>
        <ColorChangeThemePopper id={id} open={open} anchorEl={anchorEl} />
      </div>
    </div>
  );
}

export default withRouter(connect()(Layout));
