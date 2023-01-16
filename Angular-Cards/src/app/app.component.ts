import { Component, NgZone } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  public gifts: string[] = []

  public displayImage: boolean = false;
  public path: SafeResourceUrl | undefined;

  constructor(private _sanitizer: DomSanitizer, private zone: NgZone) {

      (window as any).activity.onReceiveImage = (image: string) => {
        console.log("image is");
        console.log(image)
  
        this.zone.run(() => {
          this.path = this._sanitizer.bypassSecurityTrustResourceUrl(image);
          console.log(this.path)
          this.displayImage = true
        })
    }

    this.readList();
  }

  private readList() {
    let giftString = this.getActivity().receiveList();
    this.gifts = JSON.parse(!!giftString ? giftString: "[]");
  }

  public finish() {
      this.getActivity().done();
  }

  private getActivity(): Activity {
    if (!!(window as any).activity) {
      return (window as any).activity as Activity
    }

    let activity = new Activity()
    // setup methods
    return activity
  }
}

export class Activity {
  public receiveList: () => string = () => "[]"
  public done: () => void = () => {}
}
