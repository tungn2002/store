import { lazy } from "react";

// use lazy for better code splitting, a.k.a. load faster
const Dashboard = lazy(() => import("../pages/Dashboard"));
const Orders = lazy(() => import("../pages/Orders"));
const OrderDetail = lazy(() => import("../pages/OrderDetail"));
const ProductsAll = lazy(() => import("../pages/ProductsAll"));
const SingleProduct = lazy(() => import("../pages/SingleProduct"));
const AddProduct = lazy(() => import("../pages/AddProduct"));
const EditProduct = lazy(() => import("../pages/EditProduct"));
const Customers = lazy(() => import("../pages/Customers"));
const Categories = lazy(() => import("../pages/Categories"));
const Brands = lazy(() => import("../pages/Brands"));
const Reports = lazy(() => import("../pages/Reports"));
const Chats = lazy(() => import("../pages/Chats"));
const Profile = lazy(() => import("../pages/Profile"));
const Settings = lazy(() => import("../pages/Settings"));
const Roles = lazy(() => import("../pages/Roles"));
const RolePermissions = lazy(() => import("../pages/RolePermissions"));
const Page404 = lazy(() => import("../pages/404"));
const Blank = lazy(() => import("../pages/Blank"));

/**
 * ⚠ These are internal routes!
 * They will be rendered inside the app, using the default `containers/Layout`.
 * If you want to add a route to, let's say, a landing page, you should add
 * it to the `App`'s router, exactly like `Login`, `CreateAccount` and other pages
 * are routed.
 *
 * If you're looking for the links rendered in the SidebarContent, go to
 * `routes/sidebar.js`
 */
const routes = [
  {
    path: "/dashboard", // the url
    component: Dashboard,
  },
  {
    path: "/orders",
    component: Orders,
  },
  {
    path: "/order/:id",
    component: OrderDetail,
  },
  {
    path: "/all-products",
    component: ProductsAll,
  },
  {
    path: "/add-product",
    component: AddProduct,
  },
  {
    path: "/products/:id",
    component: SingleProduct,
  },
  {
    path: "/products/:id/edit",
    component: EditProduct,
  },
  {
    path: "/customers",
    component: Customers,
  },
  {
    path: "/categories",
    component: Categories,
  },
  {
    path: "/brands",
    component: Brands,
  },
  {
    path: "/reports",
    component: Reports,
  },
  {
    path: "/roles",
    component: Roles,
  },
  {
    path: "/role-permissions/:id",
    component: RolePermissions,
  },
  {
    path: "/chats",
    component: Chats,
  },
  {
    path: "/manage-profile",
    component: Profile,
  },
  {
    path: "/settings",
    component: Settings,
  },
  {
    path: "/404",
    component: Page404,
  },
  {
    path: "/blank",
    component: Blank,
  },
];

export default routes;
