(function () {
    var originalFetch = window.fetch;

    function getCsrfToken() {
        var match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
        return match ? decodeURIComponent(match[1]) : null;
    }

    window.fetch = function (url, options) {
        options = options || {};
        var method = (options.method || 'GET').toUpperCase();

        if (!options.credentials) {
            options.credentials = 'include';
        }

        if (method !== 'GET' && method !== 'HEAD') {
            var token = getCsrfToken();
            if (token) {
                if (!options.headers) {
                    options.headers = {};
                }
                if (options.headers instanceof Headers) {
                    options.headers.set('X-XSRF-TOKEN', token);
                } else {
                    options.headers['X-XSRF-TOKEN'] = token;
                }
            }
        }

        return originalFetch.call(this, url, options);
    };
})();
