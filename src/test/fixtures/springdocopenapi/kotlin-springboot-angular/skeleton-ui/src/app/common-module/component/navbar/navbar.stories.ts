import {HttpClientModule} from '@angular/common/http';
import {importProvidersFrom} from '@angular/core';
import {RouterModule} from '@angular/router';
import {ApiModule} from '@api';
import {ClfCommonModule} from '@common/clf-common.module';
import {NavbarComponent} from '@common/component/navbar/navbar.component';
import {TranslocoModule} from '@ngneat/transloco';
import {applicationConfig, type Meta, moduleMetadata, type StoryObj} from '@storybook/angular';

// More on how to set up stories at: https://storybook.js.org/docs/angular/writing-stories/introduction
const meta: Meta<NavbarComponent> = {
    title: 'Component/NavbarComponent',
    component: NavbarComponent,
    tags: ['autodocs'],
    decorators: [
        applicationConfig({
            providers: [importProvidersFrom(TranslocoModule), importProvidersFrom(HttpClientModule), importProvidersFrom(ApiModule)],
        }),
        moduleMetadata({
            imports: [RouterModule, ClfCommonModule],
        }),
    ],
};

export default meta;
type Story = StoryObj<NavbarComponent>;

// More on writing stories with args: https://storybook.js.org/docs/angular/writing-stories/args
export const Default: Story = {};
