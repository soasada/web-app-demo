<template>
    <div id="app">
        <nav class="navbar" role="navigation" aria-label="main navigation">
            <div class="container">
                <div class="navbar-brand">
                    <router-link class="navbar-item" to="/">
                        <img src="./assets/logo.png" width="28" height="28" alt="Company logo">
                    </router-link>

                    <a role="button" class="navbar-burger burger" aria-label="menu" aria-expanded="false"
                       data-target="navbarMainMenu" @click="toggleNav">
                        <span aria-hidden="true"></span>
                        <span aria-hidden="true"></span>
                        <span aria-hidden="true"></span>
                    </a>
                </div>

                <div id="navbarMainMenu" class="navbar-menu is-active">
                    <div class="navbar-start">
                        <router-link class="navbar-item" active-class="is-active is-tab" to="/" exact>Home</router-link>
                        <router-link class="navbar-item" active-class="is-active is-tab" to="/about">About</router-link>
                        <router-link class="navbar-item" active-class="is-active is-tab" to="/users">Users</router-link>
                        <router-link class="navbar-item" active-class="is-active is-tab" to="/houses">Houses</router-link>
                        <router-link class="navbar-item" active-class="is-active is-tab" to="/furniture">Furniture</router-link>
                    </div>

                    <div class="navbar-end">
                        <div class="navbar-item">
                            <div class="buttons" v-if="$store.getters.isAuthenticated">
                                <a class="button is-primary" @click="logout">
                                    <strong>Log out</strong>
                                </a>
                            </div>
                            <div class="buttons" v-else>
                                <a class="button is-primary">
                                    <strong>Sign up</strong>
                                </a>
                                <router-link class="button is-light" to="/login">Log in</router-link>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </nav>

        <main class="section">
            <router-view/>
        </main>

        <footer class="footer">
            <div class="container">
                <div class="content has-text-centered">
                    <p>
                        <strong>Undertow + Vue.js demo</strong> by <a href="https://github.com/soasada">Nicolás Vargas
                        Ortega</a>. The source code is licensed
                        <a href="http://opensource.org/licenses/mit-license.php">MIT</a>. © {{ new Date().getFullYear()
                        }}
                        Popokis.com
                    </p>
                </div>
            </div>
        </footer>
    </div>
</template>

<script>
    export default {
        name: 'app',
        methods: {
            toggleNav() {
                const $navbarBurgers = Array.prototype.slice.call(document.querySelectorAll('.navbar-burger'), 0);

                if ($navbarBurgers.length > 0) {
                    $navbarBurgers.forEach(el => {
                        el.addEventListener('click', () => {
                            const target = el.dataset.target;
                            const $target = document.getElementById(target);

                            el.classList.toggle('is-active');
                            $target.classList.toggle('is-active');
                        });
                    });
                }
            },
            logout() {
                this.$store.dispatch('signOut');
                this.$router.push('home');
            }
        }
    }
</script>

<style lang="scss">

</style>
