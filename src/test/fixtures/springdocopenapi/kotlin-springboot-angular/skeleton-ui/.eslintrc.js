module.exports = {
    root: true,
    env: {
        es6: true,
        node: true,
    },
    plugins: ['@cloudflight/typescript'],
    extends: ['plugin:@cloudflight/angular/recommended', 'plugin:storybook/recommended'],
    parserOptions: {
        project: ['tsconfig.json', 'tsconfig.spec.json', 'tsconfig.app.json'],
        sourceType: 'module',
    },
    settings: {
        'import/resolver': {
            typescript: {
                alwaysTryTypes: true,
                project: ['tsconfig.json', 'tsconfig.spec.json', 'tsconfig.app.json'],
            },
        },
    },
};
