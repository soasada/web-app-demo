import Vue from 'vue'
import Router from 'vue-router'
import Home from './views/Home.vue'
import Users from './views/Users.vue'
import Houses from './views/Houses.vue'
import Furniture from './views/Furniture.vue'
import Login from './views/Login.vue'

Vue.use(Router);

export default new Router({
    mode: 'history',
    base: process.env.BASE_URL,
    routes: [
        {
            path: '/',
            name: 'home',
            component: Home
        },
        {
            path: '/about',
            name: 'about',
            // route level code-splitting
            // this generates a separate chunk (about.[hash].js) for this route
            // which is lazy-loaded when the route is visited.
            component: () => import(/* webpackChunkName: "about" */ './views/About.vue')
        },
        {
            path: '/users',
            name: 'users',
            component: Users,
            meta: {requiresAuth: true}
        },
        {
            path: '/houses',
            name: 'houses',
            component: Houses,
            meta: {requiresAuth: true}
        },
        {
            path: '/furniture',
            name: 'furniture',
            component: Furniture,
            meta: {requiresAuth: true}
        },
        {
            path: '/login',
            name: 'login',
            component: Login
        }
    ]
});
