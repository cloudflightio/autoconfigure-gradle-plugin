const {pathsToModuleNameMapper} = require('ts-jest');
const Config = require('./tsconfig.json');

module.exports = {
    preset: 'jest-preset-angular',
    roots: ['<rootDir>/src/'],
    testMatch: ['**/+(*.)+(spec).+(ts)'],
    setupFilesAfterEnv: ['<rootDir>/src/test.ts'],

    collectCoverage: true,
    coverageReporters: ['html'],
    coverageDirectory: 'coverage/skeleton-ui',

    moduleNameMapper: pathsToModuleNameMapper(Config.compilerOptions.paths, {prefix: '<rootDir>'}),
    moduleFileExtensions: ['ts', 'js', 'html', 'svg'],

    testResultsProcessor: 'jest-teamcity-reporter',
    transform: {
        '^.+\\.(ts|mjs|js|html)$': [
            'jest-preset-angular',
            {
                tsconfig: '<rootDir>/tsconfig.spec.json',
                stringifyContentPathRegex: '\\.(html|svg)$',
            },
        ],
    },
};
