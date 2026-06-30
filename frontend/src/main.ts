import { createPinia } from 'pinia'
import { createApp } from 'vue'

import App from './App.vue'
import router from './router'
import './styles/global.css'

function isInsideImageViewer(target: EventTarget | null) {
  return target instanceof Element && Boolean(target.closest('.article-image-viewer'))
}

function preventDocumentPinchZoom(event: TouchEvent) {
  if (event.touches.length < 2 || isInsideImageViewer(event.target)) return

  event.preventDefault()
}

function preventDocumentGestureZoom(event: Event) {
  if (isInsideImageViewer(event.target)) return

  event.preventDefault()
}

document.addEventListener('touchmove', preventDocumentPinchZoom, { passive: false })
document.addEventListener('gesturestart', preventDocumentGestureZoom, { passive: false })
document.addEventListener('gesturechange', preventDocumentGestureZoom, { passive: false })

createApp(App).use(createPinia()).use(router).mount('#app')
