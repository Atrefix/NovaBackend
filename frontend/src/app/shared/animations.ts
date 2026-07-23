import { trigger, transition, style, query, animate, group } from '@angular/animations';

export const pageTransitions = trigger('routeAnimations', [
  transition('* <=> *', [
    style({ position: 'relative' }),
    query(':enter, :leave', [
      style({
        position: 'absolute',
        top: 0,
        left: 0,
        width: '100%',
        opacity: 0,
      })
    ], { optional: true }),
    query(':enter', [
      style({ transform: 'translateY(15px)', opacity: 0 })
    ], { optional: true }),
    group([
      query(':leave', [
        animate('300ms ease-out', style({ transform: 'translateY(-15px)', opacity: 0 }))
      ], { optional: true }),
      query(':enter', [
        animate('400ms 100ms ease-out', style({ transform: 'translateY(0)', opacity: 1 }))
      ], { optional: true })
    ])
  ])
]);

export const fadeInUp = trigger('fadeInUp', [
  transition(':enter', [
    style({ transform: 'translateY(20px)', opacity: 0 }),
    animate('400ms cubic-bezier(0.16, 1, 0.3, 1)', style({ transform: 'translateY(0)', opacity: 1 }))
  ])
]);

export const fadeIn = trigger('fadeIn', [
  transition(':enter', [
    style({ opacity: 0 }),
    animate('250ms ease-out', style({ opacity: 1 }))
  ]),
  transition(':leave', [
    animate('200ms ease-in', style({ opacity: 0 }))
  ])
]);

export const slideInOut = trigger('slideInOut', [
  transition(':enter', [
    style({ transform: 'translateX(100%)' }),
    animate('300ms cubic-bezier(0.16, 1, 0.3, 1)', style({ transform: 'translateX(0)' }))
  ]),
  transition(':leave', [
    animate('250ms cubic-bezier(0.7, 0, 0.84, 0)', style({ transform: 'translateX(100%)' }))
  ])
]);
